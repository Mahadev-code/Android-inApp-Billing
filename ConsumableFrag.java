package com.shiv.shambhu.fragment.billing;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;
import com.shiv.shambhu.R;
import com.shiv.shambhu.config.AppConfig;

import java.util.ArrayList;
import java.util.List;

import games.moisoni.google_iab.BillingConnector;
import games.moisoni.google_iab.BillingEventListener;
import games.moisoni.google_iab.models.BillingResponse;
import games.moisoni.google_iab.models.PurchaseInfo;
import games.moisoni.google_iab.models.SkuInfo;

public class ConsumableFrag extends Fragment {

    private BillingConnector billingConnector;

    //create a list with consumable ids
    private final List<String> consumableIds = new ArrayList<>();

    private final ArrayList<String> purchaseItemDisplay = new ArrayList<>();
    private ArrayAdapter<String> arrayAdapter;
    private ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View view = inflater.inflate(R.layout.layout, container, false);
        initializeBillingClient();
        initViews(view);
        clickListeners();
        return view;
    }

    private void initializeBillingClient() {

        consumableIds.add("consumable_id1");
        consumableIds.add("consumable_id2");
        consumableIds.add("consumable_id3");

        billingConnector = new BillingConnector(getContext(), "license_key") //"license_key" - public developer key from Play Console
                .setConsumableIds(consumableIds) //to set consumable ids - call only for consumable products
                .autoAcknowledge() //legacy option - better call this. Alternatively purchases can be acknowledge via public method "acknowledgePurchase(PurchaseInfo purchaseInfo)"
                .autoConsume() //legacy option - better call this. Alternatively purchases can be consumed via public method consumePurchase(PurchaseInfo purchaseInfo)"
                .enableLogging() //to enable logging for debugging throughout the library - this can be skipped
                .connect(); //to connect billing client with Play Console

        billingConnector.setBillingEventListener(new BillingEventListener() {
            @Override
            public void onProductsFetched(@NonNull List<SkuInfo> skuDetails) {

                notifyList(skuDetails);
            }

            @Override
            public void onPurchasedProductsFetched(@NonNull List<PurchaseInfo> purchases) {

            }

            @Override
            public void onProductsPurchased(@NonNull List<PurchaseInfo> purchases) {
                String skuName;
                for (PurchaseInfo purchaseInfo : purchases) {
                    skuName = purchaseInfo.getSkuInfo().getTitle();
                    Toast.makeText(getActivity(), "Product purchased : " + skuName, Toast.LENGTH_SHORT).show();
                }
                
            }

            @Override
            public void onPurchaseAcknowledged(@NonNull PurchaseInfo purchase) {
                /*
                 * Grant user entitlement for NON-CONSUMABLE products and SUBSCRIPTIONS here
                 *
                 * Even though onProductsPurchased is triggered when a purchase is successfully made
                 * there might be a problem along the way with the payment and the purchase won't be acknowledged
                 *
                 * Google will refund users purchases that aren't acknowledged in 3 days
                 *
                 * To ensure that all valid purchases are acknowledged the library will automatically
                 * check and acknowledge all unacknowledged products at the startup
                 * */

                String acknowledgedSku = purchase.getSku();
                Log.d("BillingConnector", "Acknowledged: " + acknowledgedSku);
                Toast.makeText(getActivity(), "Acknowledged : " + acknowledgedSku, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPurchaseConsumed(@NonNull PurchaseInfo purchase) {
                /*
                 * CONSUMABLE products entitlement can be granted either here or in onProductsPurchased
                 * */

                String consumedSkuTitle = purchase.getSkuInfo().getTitle();
                Log.d("BillingConnector", "Consumed: " + consumedSkuTitle);
                Toast.makeText(getActivity(), "Consumed : " + consumedSkuTitle, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBillingError(@NonNull BillingConnector billingConnector, @NonNull BillingResponse response) {
                switch (response.getErrorType()) {
                    case CLIENT_NOT_READY:
                        //TODO - client is not ready yet
                        break;
                    case CLIENT_DISCONNECTED:
                        //TODO - client has disconnected
                        break;
                    case SKU_NOT_EXIST:
                        //TODO - sku does not exist
                        break;
                    case CONSUME_ERROR:
                        //TODO - error during consumption
                        break;
                    case ACKNOWLEDGE_ERROR:
                        //TODO - error during acknowledgment
                        break;
                    case ACKNOWLEDGE_WARNING:
                        /*
                         * This will be triggered when a purchase can not be acknowledged because the state is PENDING
                         * A purchase can be acknowledged only when the state is PURCHASED
                         *
                         * PENDING transactions usually occur when users choose cash as their form of payment
                         *
                         * Here users can be informed that it may take a while until the purchase complete
                         * and to come back later to receive their purchase
                         * */
                        //TODO - warning during acknowledgment
                        break;
                    case FETCH_PURCHASED_PRODUCTS_ERROR:
                        //TODO - error occurred while querying purchased products
                        break;
                    case BILLING_ERROR:
                        //TODO - error occurred during initialization / querying sku details
                        break;
                    case USER_CANCELED:
                        //TODO - user pressed back or canceled a dialog
                        break;
                    case SERVICE_UNAVAILABLE:
                        //TODO - network connection is down
                        break;
                    case BILLING_UNAVAILABLE:
                        //TODO - billing API version is not supported for the type requested
                        break;
                    case ITEM_UNAVAILABLE:
                        //TODO - requested product is not available for purchase
                        break;
                    case DEVELOPER_ERROR:
                        //TODO - invalid arguments provided to the API
                        break;
                    case ERROR:
                        //TODO - fatal error during the API action
                        break;
                    case ITEM_ALREADY_OWNED:
                        //TODO - failure to purchase since item is already owned
                        break;
                    case ITEM_NOT_OWNED:
                        //TODO - failure to consume since item is not owned
                        break;
                }

                Log.d("BillingConnector", "Error type: " + response.getErrorType() +
                        " Response code: " + response.getResponseCode() + " Message: " + response.getDebugMessage());

                Toast.makeText(getContext(), "Error type: " + response.getErrorType() +
                        " Response code: " + response.getResponseCode() + " Message: " + response.getDebugMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void notifyList(List<SkuInfo> skuDetails) {
        String sku;
        String title;
        String price;

        purchaseItemDisplay.clear();
        for (SkuInfo skuInfo : skuDetails) {
            sku = skuInfo.getSku();
            title = skuInfo.getTitle();
            price = skuInfo.getPrice();

            purchaseItemDisplay.add(title + " : " + price);
            arrayAdapter.notifyDataSetChanged();

        }
    }

    private void initViews(View view) {

        listView = view.findViewById(R.id.list_view);
        arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, purchaseItemDisplay);
        listView.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();
    }

    private void clickListeners() {

        listView.setOnItemClickListener((parent, view, position, id) -> billingConnector.purchase(getActivity(), consumableIds.get(position)));
    }
}
