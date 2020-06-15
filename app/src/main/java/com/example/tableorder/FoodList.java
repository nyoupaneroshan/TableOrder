package com.example.tableorder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.tableorder.Common.Common;
import com.example.tableorder.Databases.Database;
import com.example.tableorder.Interface.ItemClickListener;
import com.example.tableorder.Model.Food;
import com.example.tableorder.ViewHolder.FoodViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class FoodList extends AppCompatActivity {

   RecyclerView recyclerView;
   RecyclerView.LayoutManager layoutManager;

   FirebaseDatabase database;
   DatabaseReference foodList;

   String categoryId="";
   FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;

   //search
    FirebaseRecyclerAdapter<Food,FoodViewHolder> searchAdapter;
    List<String> suggestList = new ArrayList<>();
    MaterialSearchBar materialSearchBar;

    //localDB
    Database localDB;

   protected void onCreate(Bundle savedInstanceState){
       super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_food_list);

       //firebase connection
       database = FirebaseDatabase.getInstance();
       foodList = database.getReference("Food");

       localDB = new Database(this);

       recyclerView =(RecyclerView)findViewById(R.id.recycler_food);
       recyclerView.setHasFixedSize(true);
       layoutManager = new LinearLayoutManager(this);
       recyclerView.setLayoutManager(layoutManager);

       //get instance here

       if(getIntent() != null)
           categoryId = getIntent().getStringExtra("CategoryId");

        if(!categoryId.isEmpty() && categoryId !=null){
            if (Common.isConnectedToInternet(getBaseContext()))
            loadListFood(categoryId);
            else {
                Toast.makeText(FoodList.this,"No Internet Connection !!!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        //search
       materialSearchBar = findViewById(R.id.searchBar);
        materialSearchBar.setHint("Enter Food name");

        loadSuggest();
        materialSearchBar.setLastSuggestions(suggestList);
        materialSearchBar.setCardViewElevation(10);
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //changing suggestion as the the text typed by the user
                List<String> suggest = new ArrayList<>();
                for (String search:suggestList){ //loop in suggest list
                    if (search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                        suggest.add(search);
                }
                materialSearchBar.setLastSuggestions(suggest);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                //when search closed
                if (!enabled){
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
            //after search finish
                startSearch(text);
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });

   }

   private void startSearch(CharSequence text){
       searchAdapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(
               Food.class,
               R.layout.food_item,
               FoodViewHolder.class,
               foodList.orderByChild("Name").equalTo(text.toString()) //compare name
       ) {
           @Override
           protected void populateViewHolder(FoodViewHolder viewHolder, Food model, int position) {
               viewHolder.food_name.setText(model.getName());
               Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.food_image);

               final Food  local = model;
               viewHolder.setItemClickListener(new ItemClickListener() {
                   @Override
                   public void onClick(View view, int position, boolean isLongClick) {
                       //Toast.makeText(FoodList.this, ""+local.getName(), Toast.LENGTH_SHORT).show();
                       Intent foodDetail = new Intent(FoodList.this, FoodDetail.class);
                       foodDetail.putExtra("FoodId", searchAdapter.getRef(position).getKey());
                       startActivity(foodDetail);
                        }});
           }
       };
       recyclerView.setAdapter(searchAdapter); //set adapter for recycle view to show search result
   }

    private void loadSuggest() {
       foodList.orderByChild("MenuId").equalTo(categoryId)
               .addValueEventListener(new ValueEventListener() {
                   @Override
                   public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                       for (DataSnapshot postSnapshot:dataSnapshot.getChildren()){
                           Food item = postSnapshot.getValue(Food.class);
                           suggestList.add(item.getName()); //retrive sggested food in search bar
                       }
                   }

                   @Override
                   public void onCancelled(@NonNull DatabaseError databaseError) {

                   }
               });
    }

    private void loadListFood(String categoryId) {
       adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(Food.class,
               R.layout.food_item,
               FoodViewHolder.class,
               foodList.orderByChild("menuId").equalTo(categoryId)) {
           @Override
           protected void populateViewHolder(final FoodViewHolder viewHolder, final Food model, final int position) {
            viewHolder.food_name.setText(model.getName());
            Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.food_image);

            //adding favourites
               if (localDB.isFavourites(adapter.getRef(position).getKey()))
                   viewHolder.fav_image.setImageResource(R.drawable.ic_baseline_favorite_border_24);

               viewHolder.fav_image.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       if (!localDB.isFavourites(adapter.getRef(position).getKey())){
                           localDB.addToFavourites(adapter.getRef(position).getKey());
                           viewHolder.fav_image.setImageResource(R.drawable.ic_baseline_favorite_24);
                           Toast.makeText(FoodList.this, ""+model.getName()+" was addeed to favourites", Toast.LENGTH_SHORT).show();
                       }
                       else{
                           localDB.removeFavourites(adapter.getRef(position).getKey());
                           viewHolder.fav_image.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                           Toast.makeText(FoodList.this, ""+model.getName()+" is removed favourites", Toast.LENGTH_SHORT).show();
                       }
                   }
               });

           final Food  local = model;
           viewHolder.setItemClickListener(new ItemClickListener() {
               @Override
               public void onClick(View view, int position, boolean isLongClick) {
                   //Toast.makeText(FoodList.this, ""+local.getName(), Toast.LENGTH_SHORT).show();
                    Intent foodDetail = new Intent(FoodList.this,FoodDetail.class);
                    foodDetail.putExtra("FoodId", adapter.getRef(position).getKey());
                    startActivity(foodDetail);
               }
           });
           }
       };
       recyclerView.setAdapter(adapter);
    }
}
