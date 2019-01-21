package com.example.user.paynow.underground;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.user.paynow.R;

import java.util.ArrayList;
import java.util.List;

/*
* Recycler View의 ListAdapter - ShoppingActivity와 CompleteListActicity의 View에 모두 적용
*/

public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.CustomViewHolder> {

    NetworkData networkData = new NetworkData();
    String IP_ADDRESS = networkData.IP_ADDRESS;
    String TAG = networkData.TAG;

    private ArrayList<ProductData> mList = null;
    private String mJsonString;

    private Activity context = null;
    private int position=-1;
    ArrayList<Integer> selectList = new ArrayList<Integer>();
  //  ArrayList<ProductData> totalList = new ArrayList<ProductData>();

    public RecycleViewAdapter(Activity context, ArrayList<ProductData> list) {   //ArrayList를 List로변경해봄
        this.context = context;
        this.mList = list;
//        totalList.addAll(list);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener{
        protected TextView id;
        protected TextView name;
        protected TextView price;

        @Override
        public boolean onLongClick(View view) {
            // Handle long click
            // Return true to indicate the click was handled
            /*현재 가져와서 삭제*/
            int position = getAdapterPosition();

            Log.d("LONGCLICK", "event occur");
            //mList.remove(aPosition);
            notifyItemRemoved(position);
            notifyDataSetChanged();
            notifyItemRangeChanged(position, mList.size());
            return true;
        }


        public CustomViewHolder(View view) {
            super(view);
            this.id = (TextView) view.findViewById(R.id.textView_list_code);
            this.name = (TextView) view.findViewById(R.id.textView_list_name);
            this.price = (TextView) view.findViewById(R.id.textView_list_price);
        }

    }


    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list, null);
        final CustomViewHolder viewHolder = new CustomViewHolder(view);

        //onClick Event 발생할 때마다 adapterPosition가져오고 List에 변동사항 반영
        viewHolder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                position = viewHolder.getAdapterPosition();
                onClickDelete(position);    //alertDialog띄우고 데이터 삭제여부 묻는애

            }
        });


        return viewHolder;
    }


    //ClickEvent
    @Override
    public void onBindViewHolder(@NonNull final CustomViewHolder viewholder, int position) {

        final int pos = position;
        viewholder.id.setText(mList.get(position).getProduct_code());
        viewholder.name.setText(mList.get(position).getProduct_name());
        viewholder.price.setText(mList.get(position).getProduct_price());
    }

    @Override
    public int getItemCount() {
        return (null != mList ? mList.size() : 0);
    }

    public ArrayList<Integer> getSelectList(){
        return selectList;
    }


    public void onClickDelete(final int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("상품을 삭제하시겠습니까?");
        builder.setMessage(mList.get(position).getProduct_name());
        builder.setPositiveButton("확인",
                new DialogInterface.OnClickListener() {         //제대로 돌아가는 거 확인
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("DELETE", "called");
                        String code = mList.get(position).getProduct_code();    //상품 코드를 따온다

                        //DB에서 삭제->!!!!!!!!!삭제 처리는 되는데 반영이 바로 안 됨 왜..?
                        //아개빠가네 상품코드로 아이디찾으면 어캄
                        InsertData task = new InsertData(context);
                        task.execute("http://" + IP_ADDRESS + "/deleteShoppingList.php", code);  //해당 아이디를 shoppingList에서 삭제
                        Log.d("DELETE", "complete");

                        mList.remove(position); //리스트에서도 지워줌
                        notifyDataSetChanged();
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, mList.size());

                        dialog.dismiss();

                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {         //
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.show();
    }

    public int getTotalPrice(){
        int totalPrice = 0;
        for(ProductData productData : mList){   //삭제된 애가 리스트에서도 삭제되는지 확인
            totalPrice += Integer.parseInt(productData.getProduct_price());
        }
        return totalPrice;
    }



}