package com.example.tableorder;

import android.content.Intent;
import android.os.Bundle;

import com.example.tableorder.Common.Common;
import com.example.tableorder.Interface.ItemClickListener;
import com.example.tableorder.Model.Category;
import com.example.tableorder.Model.Token;
import com.example.tableorder.ViewHolder.MenuViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import io.paperdb.Paper;

public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "";
    private AppBarConfiguration mAppBarConfiguration;

    FirebaseDatabase database;
    DatabaseReference category, users;

    TextView txtFullNam;
    String user;

    RecyclerView recycler_menu;
    RecyclerView.LayoutManager layoutManager;
    FirebaseRecyclerAdapter<Category, MenuViewHolder> adapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle((CharSequence) "Menu");
        setSupportActionBar(toolbar);


        FirebaseDatabase instance = FirebaseDatabase.getInstance();
        database = instance;
        category = instance.getReference("Category");
        users = this.database.getReference("User");

        Paper.init(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cartIntent = new Intent(Home.this,Cart.class);
                startActivity(cartIntent);
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//        this.mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home, R.id.nav_fav, R.id.nav_order, R.id.nav_cart, R.id.nav_history, R.id.nav_signout).setDrawerLayout((DrawerLayout) findViewById(R.id.drawer_layout)).build();
//        NavigationUI.setupActionBarWithNavController((AppCompatActivity) this, navController, this.mAppBarConfiguration);
//        NavigationUI.setupWithNavController(navigationView, navController);

        ActionBarDrawerToggle toogle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toogle);
        toogle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        TextView textView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.txtFullname);
        this.txtFullNam = textView;
        textView.setText(Common.currentUser.getName());
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_menu);
        this.recycler_menu = recyclerView;
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        this.layoutManager = linearLayoutManager;
        this.recycler_menu.setLayoutManager(linearLayoutManager);

        if (Common.isConnectedToInternet(this))
        loadMenu();
        else {
            Toast.makeText(Home.this,"No Internet Connection !!!", Toast.LENGTH_SHORT).show();
            return;
        }

        updateToken(FirebaseInstanceId.getInstance().getToken());

    }

    private void updateToken(String token) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("Tokens");
        Token data = new Token(token, false);
        tokens.child(Common.currentUser.getPhone()).setValue(data);
    }

    private void loadMenu(){
        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(Category.class, R.layout.menu_item,MenuViewHolder.class,category){
            protected void populateViewHolder(MenuViewHolder viewHolder, Category model, int position){
                viewHolder.txtMenuName.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.imageView);
                final Category clickItem = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        //Toast.makeText(Home.this,""+clickItem.getName(),Toast.LENGTH_SHORT).show();
                        Intent foodList = new Intent(Home.this, FoodList.class);
                        foodList.putExtra("CategoryId", adapter.getRef(position).getKey());
                        startActivity(foodList);
                    }
                });

            }
        };
        recycler_menu.setAdapter(adapter);
    }

    public void onBackPressed(){
        DrawerLayout drawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }



    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId() == R.id.refresh)
            loadMenu();
        return  super.onOptionsItemSelected(item);
    }
    @SuppressWarnings("StatementWithEmptyBody")


    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id== R.id.nav_home){
            Intent cartIntent = new Intent(Home.this,Cart.class);
            startActivity(cartIntent);

        }
        else if (id==R.id.nav_fav){
            Intent cartIntent = new Intent(Home.this,Cart.class);
            startActivity(cartIntent);

        }
        else if(id==R.id.nav_order){
            Intent orders = new Intent(Home.this, OrderStatus.class);
            startActivity(orders);
        }
        else if (id==R.id.nav_cart){
            Intent orderIntent = new Intent(Home.this, Cart.class);
            startActivity(orderIntent);

        }
        else if (id==R.id.nav_history){

        }
        else if (id==R.id.nav_signout){
            //delete the user and password
            Paper.book().destroy();

            Intent signIn = new Intent(Home.this, SignIn.class);
            signIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(signIn);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }
}
