package com.example.tableorder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Trace;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.tableorder.Common.Common;
import com.example.tableorder.Databases.Database;
import com.example.tableorder.Model.Food;
import com.example.tableorder.Model.Order;
import com.example.tableorder.Model.Rating;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rey.material.widget.SnackBar;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class FoodDetail extends AppCompatActivity implements RatingDialogListener {

    TextView food_name, food_price, food_description;
    ImageView food_img;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton btnCart,btnRating;
    ElegantNumberButton numberButton;
    RatingBar ratingBar;

    String foodId="";

    FirebaseDatabase database;
    DatabaseReference foods;
    DatabaseReference ratingTbl;
    Food currentFood;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        //Firebase COnnection
        database = FirebaseDatabase.getInstance();
        foods= database.getReference("Food");
        ratingTbl=database.getReference("Rating");


        //Text View
        numberButton = (ElegantNumberButton)findViewById(R.id.number_button);
        btnCart = (FloatingActionButton)findViewById(R.id.btnCart);
        btnRating=findViewById(R.id.btnRate);
        ratingBar= findViewById(R.id.ratingBar);

        btnRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRatingDialog();
            }
        });

        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Database(getBaseContext()).addToCart(new Order(
                        foodId,
                        currentFood.getName(),
                        numberButton.getNumber(),
                        currentFood.getPrice(),
                        currentFood.getDiscount()
                ));
                Toast.makeText(FoodDetail.this,"Added to Cart", Toast.LENGTH_SHORT).show();
            }
        });

        food_description= (TextView) findViewById(R.id.food_description);
        food_name = (TextView) findViewById(R.id.food_name);
        food_img = (ImageView)findViewById(R.id.img_food);
        food_price= (TextView)findViewById(R.id.food_price);

        collapsingToolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.collapsing);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);

        if(getIntent() != null)
            foodId = getIntent().getStringExtra("FoodId");
            if (!foodId.isEmpty()){
                if (Common.isConnectedToInternet(getBaseContext())){
                    getDetailFood(foodId);

                    getRatingFood(foodId);
                }
                else {
                    Toast.makeText(FoodDetail.this,"No Internet Connection !!!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
    }

    private void getRatingFood(String foodId) {
        Query foodRating = ratingTbl.orderByChild("foodId").equalTo(foodId);

        foodRating.addValueEventListener(new ValueEventListener() {

            int count =0, sum=0;
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot:dataSnapshot.getChildren()){
                    Rating item = postSnapshot.getValue(Rating.class);
                    sum+=Integer.parseInt(item.getRateValue());
                    count++;
                }

                if (count!=0){
                    float average = sum/count;
                    ratingBar.setRating(average);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showRatingDialog() {
        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNoteDescriptions(Arrays.asList("Very Bad", "Not Good", "Quite Good", "Best Food Ever"))
        .setDefaultRating(1)
        .setTitle("Rate This Food!")
        .setDescription("Select the rating and provide your feedback")
        .setTitleTextColor(R.color.colorPrimary)
        .setDescriptionTextColor(R.color.colorPrimary)
        .setHint("Comments here!!!")
        .setHintTextColor(R.color.colorAccent)
        .setCommentTextColor(R.color.colorPrimaryDark)
        .setWindowAnimation(R.style.RatingDialogAnim)
        .create(FoodDetail.this)
        .show();
    }

    private void getDetailFood(String foodId) {
        foods.child(foodId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentFood = dataSnapshot.getValue(Food.class);

               Picasso.with(getBaseContext()).load(currentFood.getImage()).into(food_img);
                collapsingToolbarLayout.setTitle(currentFood.getName());

                food_price.setText(currentFood.getPrice());
                food_name.setText(currentFood.getName());
                food_description.setText(currentFood.getDescription());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onNegativeButtonClicked() {

    }

    @Override
    public void onPositiveButtonClicked(int i, @NotNull String s) {
        final Rating  rating = new Rating(Common.currentUser.getPhone(), foodId,
                                          String.valueOf(i),
                                          s);

        ratingTbl.child(Common.currentUser.getPhone()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(Common.currentUser.getPhone()).exists()){
                    //remove older value
                    ratingTbl.child(Common.currentUser.getPhone()).removeValue();
                    //add new value
                    ratingTbl.child(Common.currentUser.getPhone()).setValue(rating);
                }
                else {
                    ratingTbl.child(Common.currentUser.getPhone()).setValue(rating);
                }


                 Toast.makeText(FoodDetail.this,"Thankyou for your feedback", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}

