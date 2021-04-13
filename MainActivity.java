package com.programtown.example;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.android.billingclient.api.BillingClient.SkuType.INAPP;

public class MainActivity extends AppCompatActivity implements PurchasesUpdatedListener {

    public static final String PREF_FILE= "MyPref";
    //note add unique product ids
    //use same id for preference key
    private static ArrayList<String> purchaseItemIDs = new ArrayList<String>() {{
        add("p1");
        add("p2");
        add("p3");
    }};
    private static ArrayList<String> purchaseItemDisplay = new ArrayList<String>();
    ArrayAdapter<String> arrayAdapter;
    ListView listView;

    private BillingClient billingClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView=(ListView) findViewById(R.id.listview);

        // Establish connection to billing client
        //check purchase status from google play store cache on every app start
        billingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases().setListener(this).build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {

                if(billingResult.getResponseCode()==BillingClient.BillingResponseCode.OK){
                    Purchase.PurchasesResult queryPurchase = billingClient.queryPurchases(INAPP);
                    List<Purchase> queryPurchases = queryPurchase.getPurchasesList();
                    if(queryPurchases!=null && queryPurchases.size()>0){
                        handlePurchases(queryPurchases);
                    }

                    //check which items are in purchase list and which are not in purchase list
                    //if items that are found add them to purchaseFound
                    //check status of found items and save values to preference
                    //item which are not found simply save false values to their preference
                    //indexOf return index of item in purchase list from 0-2 (because we have 3 items) else returns -1 if not found
                    ArrayList<Integer> purchaseFound =new ArrayList<Integer> ();
                    if(queryPurchases!=null && queryPurchases.size()>0){
                        //check item in purchase list
                        for(Purchase p:queryPurchases){
                            int index=purchaseItemIDs.indexOf(p.getSku());
                            //if purchase found
                            if(index>-1)
                            {
                                purchaseFound.add(index);
                                if(p.getPurchaseState() == Purchase.PurchaseState.PURCHASED)
                                {
                                    savePurchaseItemValueToPref(purchaseItemIDs.get(index),true);
                                }
                                else{
                                    savePurchaseItemValueToPref(purchaseItemIDs.get(index),false);
                                }
                            }
                        }
                        //items that are not found in purchase list mark false
                        //indexOf returns -1 when item is not in foundlist
                        for(int i=0;i < purchaseItemIDs.size(); i++){
                            if(purchaseFound.indexOf(i)==-1){
                                savePurchaseItemValueToPref(purchaseItemIDs.get(i),false);
                            }
                        }
                    }
                    //if purchase list is empty that means no item is not purchased
                    //Or purchase is refunded or canceled
                    //so mark them all false
                    else{
                        for( String purchaseItem: purchaseItemIDs ){
                            savePurchaseItemValueToPref(purchaseItem,false);
                        }
                    }

                }

            }

            @Override
            public void onBillingServiceDisconnected() {
            }
        });


        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, purchaseItemDisplay);
        listView.setAdapter(arrayAdapter);
        notifyList();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                if(getPurchaseItemValueFromPref(purchaseItemIDs.get(position))){
                    Toast.makeText(getApplicationContext(),purchaseItemIDs.get(position)+" is Already Purchased",Toast.LENGTH_SHORT).show();
                    //selected item is already purchased
                    return;
                }
                //initiate purchase on selected product item click
                //check if service is already connected
                if (billingClient.isReady()) {
                    initiatePurchase(purchaseItemIDs.get(position));
                }
                //else reconnect service
                else{
                    billingClient = BillingClient.newBuilder(MainActivity.this).enablePendingPurchases().setListener(MainActivity.this).build();
                    billingClient.startConnection(new BillingClientStateListener() {
                        @Override
                        public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                initiatePurchase(purchaseItemIDs.get(position));
                            } else {
                                Toast.makeText(getApplicationContext(),"Error "+billingResult.getDebugMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onBillingServiceDisconnected() {
                        }
                    });
                }
            }
        });
    }

    private void notifyList(){
        purchaseItemDisplay.clear();
        for(String p:purchaseItemIDs){
            purchaseItemDisplay.add("Purchase Status of "+p+" = "+getPurchaseItemValueFromPref(p));
        }
        arrayAdapter.notifyDataSetChanged();
    }

    private SharedPreferences getPreferenceObject() {
        return getApplicationContext().getSharedPreferences(PREF_FILE, 0);
    }
    private SharedPreferences.Editor getPreferenceEditObject() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(PREF_FILE, 0);
        return pref.edit();
    }
    private boolean getPurchaseItemValueFromPref(String PURCHASE_KEY){
        return getPreferenceObject().getBoolean(PURCHASE_KEY,false);
    }
    private void savePurchaseItemValueToPref(String PURCHASE_KEY,boolean value){
        getPreferenceEditObject().putBoolean(PURCHASE_KEY,value).commit();
    }


    private void initiatePurchase(final String PRODUCT_ID) {
        List<String> skuList = new ArrayList<>();
        skuList.add(PRODUCT_ID);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(INAPP);
        billingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(@NonNull BillingResult billingResult,
                                                     List<SkuDetails> skuDetailsList) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            if (skuDetailsList != null && skuDetailsList.size() > 0) {
                                BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                        .setSkuDetails(skuDetailsList.get(0))
                                        .build();
                                billingClient.launchBillingFlow(MainActivity.this, flowParams);
                            }
                            else{
                                //try to add item/product id "p1" "p2" "p3" inside managed product in google play console
                                Toast.makeText(getApplicationContext(),"Purchase Item "+PRODUCT_ID+" not Found",Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    " Error "+billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        //if item newly purchased
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            handlePurchases(purchases);
        }
        //if item already purchased then check and reflect changes
        else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            Purchase.PurchasesResult queryAlreadyPurchasesResult = billingClient.queryPurchases(INAPP);
            List<Purchase> alreadyPurchases = queryAlreadyPurchasesResult.getPurchasesList();
            if(alreadyPurchases!=null){
                handlePurchases(alreadyPurchases);
            }
        }
        //if purchase cancelled
        else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Toast.makeText(getApplicationContext(),"Purchase Canceled",Toast.LENGTH_SHORT).show();
        }
        // Handle any other error msgs
        else {
            Toast.makeText(getApplicationContext(),"Error "+billingResult.getDebugMessage(),Toast.LENGTH_SHORT).show();
        }
    }
    void handlePurchases(List<Purchase>  purchases) {
        for(Purchase purchase:purchases) {

            final int index=purchaseItemIDs.indexOf(purchase.getSku());
            //purchase found
            if(index>-1) {

                //if item is purchased
                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED)
                {
                    if (!verifyValidSignature(purchase.getOriginalJson(), purchase.getSignature())) {
                        // Invalid purchase
                        // show error to user
                        Toast.makeText(getApplicationContext(), "Error : Invalid Purchase", Toast.LENGTH_SHORT).show();
                        continue;//skip current iteration only because other items in purchase list must be checked if present
                    }
                    // else purchase is valid
                    //if item is purchased and not  Acknowledged
                    if (!purchase.isAcknowledged()) {
                        AcknowledgePurchaseParams acknowledgePurchaseParams =
                                AcknowledgePurchaseParams.newBuilder()
                                        .setPurchaseToken(purchase.getPurchaseToken())
                                        .build();

                        billingClient.acknowledgePurchase(acknowledgePurchaseParams,
                                new AcknowledgePurchaseResponseListener() {
                                    @Override
                                    public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
                                        if(billingResult.getResponseCode()==BillingClient.BillingResponseCode.OK){
                                            //if purchase is acknowledged
                                            //then saved value in preference
                                            savePurchaseItemValueToPref(purchaseItemIDs.get(index),true);
                                            Toast.makeText(getApplicationContext(), purchaseItemIDs.get(index)+" Item Purchased", Toast.LENGTH_SHORT).show();
                                            notifyList();
                                        }
                                    }
                                });

                    }
                    //else item is purchased and also acknowledged
                    else {
                        // Grant entitlement to the user on item purchase
                        if(!getPurchaseItemValueFromPref(purchaseItemIDs.get(index))){
                            savePurchaseItemValueToPref(purchaseItemIDs.get(index),true);
                            Toast.makeText(getApplicationContext(), purchaseItemIDs.get(index)+" Item Purchased.", Toast.LENGTH_SHORT).show();
                            notifyList();
                        }
                    }
                }
                //if purchase is pending
                else if(  purchase.getPurchaseState() == Purchase.PurchaseState.PENDING)
                {
                    Toast.makeText(getApplicationContext(),
                            purchaseItemIDs.get(index)+" Purchase is Pending. Please complete Transaction", Toast.LENGTH_SHORT).show();
                }
                //if purchase is refunded or unknown
                else if( purchase.getPurchaseState() == Purchase.PurchaseState.UNSPECIFIED_STATE)
                {
                    //mark purchase false in case of UNSPECIFIED_STATE
                    savePurchaseItemValueToPref(purchaseItemIDs.get(index),false);
                    Toast.makeText(getApplicationContext(), purchaseItemIDs.get(index)+" Purchase Status Unknown", Toast.LENGTH_SHORT).show();
                    notifyList();
                }
            }

        }

    }


    /**
     * Verifies that the purchase was signed correctly for this developer's public key.
     * <p>Note: It's strongly recommended to perform such check on your backend since hackers can
     * replace this method with "constant true" if they decompile/rebuild your app.
     * </p>
     */
    private boolean verifyValidSignature(String signedData, String signature) {
        try {
            //for old playconsole
            // To get key go to Developer Console > Select your app > Development Tools > Services & APIs.
            //for new play console
            //To get key go to Developer Console > Select your app > Monetize > Monetization setup

            String base64Key = "Add your key here";
			
            return Security.verifyPurchase(base64Key, signedData, signature);
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(billingClient!=null){
            billingClient.endConnection();
        }
    }

}



