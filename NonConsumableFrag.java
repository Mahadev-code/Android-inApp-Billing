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

import com.genonbeta.android.framework.app.Fragment;
import com.shiv.shambhu.R;
import com.shiv.shambhu.config.AppConfig;

import java.util.ArrayList;
import java.util.List;

import games.moisoni.google_iab.BillingConnector;
import games.moisoni.google_iab.BillingEventListener;
import games.moisoni.google_iab.models.BillingResponse;
import games.moisoni.google_iab.models.PurchaseInfo;
import games.moisoni.google_iab.models.SkuInfo;

public class NonConsumableFrag extends Fragment {

    private BillingConnector billingConnector;

     //create a list with non-consumable ids
    private final List<String> nonConsumableIds = new ArrayList<>();

    private final ArrayList<String> purchaseItemDisplay = new ArrayList<>();
    private ArrayAdapter<String> arrayAdapter;
    private ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View view = inflater.inflate(R.layout.frag_non_consumable, container, false);
        initializeBillingClient();
        initViews(view);
        clickListeners();
        return view;
    }

    private void initializeBillingClient() {

        nonConsumableIds.add("com.leaf.explorer");
        nonConsumableIds.add("non_consumable_id2");
        nonConsumableIds.add("non_consumable_id3");

        billingConnector = new BillingConnector(getContext(), AppConfig.KEY_GOOGLE_PUBLIC) //"license_key" - public developer key from Play Console
                .setNonConsumableIds(nonConsumableIds) //to set non-consumable ids - call only for non-consumable products
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
                String sku;

                for (PurchaseInfo purchaseInfo : purchases) {
                    sku = purchaseInfo.getSku();

                    if (sku.equalsIgnoreCase("com.leaf.explorer")) {
                        //do something
                        Log.d("BillingConnector", "Product purchased: " + sku);
                        Toast.makeText(getActivity(), "Product purchased: " + sku, Toast.LENGTH_SHORT).show();

                    } else if (sku.equalsIgnoreCase("non_consumable_id2")) {
                        //do something
                        Log.d("BillingConnector", "Product purchased: " + sku);
                        Toast.makeText(getActivity(), "Product purchased: " + sku, Toast.LENGTH_SHORT).show();

                    } else if (sku.equalsIgnoreCase("non_consumable_id3")) {
                        //do something
                        Log.d("BillingConnector", "Product purchased: " + sku);
                        Toast.makeText(getActivity(), "Product purchased: " + sku, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onPurchaseAcknowledged(@NonNull PurchaseInfo purchase) {

            }

            @Override
            public void onPurchaseConsumed(@NonNull PurchaseInfo purchase) {

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
        String price;

        purchaseItemDisplay.clear();
        for (SkuInfo skuInfo : skuDetails) {
            sku = skuInfo.getSku();
            price = skuInfo.getPrice();

            purchaseItemDisplay.add(sku + " "+ price);
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

        listView.setOnItemClickListener((parent, view, position, id) -> billingConnector.purchase(getActivity(), nonConsumableIds.get(position)));
    }
}
