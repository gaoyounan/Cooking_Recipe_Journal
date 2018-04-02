package com.example.gaoyounan.cooking_recipe;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gaoyounan.cooking_recipe.model.Recipe;
import com.example.gaoyounan.cooking_recipe.util.RecipeImageUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private RecipeListAdapter recipeListAdapter;
    private ListView recipeListView;
    private TextView mEditText;


    private void loginAndFetchRecipeList()
    {

        mAuth.signInWithEmailAndPassword("gaoyounan@foxmail.com", "123456")
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            System.out.println("Authentication Success");
                            loadRecipeList("recipes", "");


                        } else {
                            // If sign in fails, display a message to the user.

                            System.out.println("Authentication failed");

                        }
                    }
                });

    }

    private void loadRecipeList(final String path, final String queryContent)
    {
        // Query myTopPostsQuery = FirebaseDatabase.getInstance().getReference().child("users").orderByChild("country");
        Query myTopPostsQuery = FirebaseDatabase.getInstance().getReference().child(path);

        myTopPostsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                System.out.println("Success!!!!");
                Recipe recipe = null;

                if("".equals(queryContent))
                {
                    for (DataSnapshot postSnapshot: dataSnapshot.getChildren())
                    {
                        recipe = postSnapshot.getValue(Recipe.class);
                        recipeListAdapter.addRecipe(recipe);
                    }
                }
                else
                {
                    String recipeName = null;
                    for (DataSnapshot postSnapshot: dataSnapshot.getChildren())
                    {
                        recipe = postSnapshot.getValue(Recipe.class);

                        recipeName = recipe.getRecipe_name().toLowerCase();
                        if(recipeName.indexOf(queryContent.toLowerCase()) > -1)
                        {
                            recipeListAdapter.addRecipe(recipe);
                        }
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
                System.out.println("Failure");
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recipeListView = findViewById(R.id.recipe_list);
        recipeListAdapter = new RecipeListAdapter();
        recipeListView.setAdapter(recipeListAdapter);

        recipeListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

                                                  @Override
                                                  public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                  Recipe recipe = recipeListAdapter.getRecipe(i);

                  Intent intent = new Intent();
                  intent.putExtra("recipe_name", recipe.getRecipe_name());
                  intent.setClass(MainActivity.this, RecipeDetailsActivity.class);
                  startActivity(intent);
                  //如果不关闭当前的会出现好多个页面
                  MainActivity.this.finish();

              }
          });



        mEditText = findViewById(R.id.search_input);
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {


            @Override
            public boolean onEditorAction(TextView editTextView, int actionId, KeyEvent event) {
                //当actionId == XX_SEND 或者 XX_DONE时都触发
                //或者event.getKeyCode == ENTER 且 event.getAction == ACTION_DOWN时也触发
                //注意，这是一定要判断event != null。因为在某些输入法上会返回null。
                if (actionId == EditorInfo.IME_ACTION_SEND
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || (event != null && KeyEvent.KEYCODE_ENTER == event.getKeyCode() && KeyEvent.ACTION_DOWN == event.getAction())) {
                    //处理事件


                    editTextView.clearFocus();
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editTextView.getWindowToken(),0);

                    String search_content = editTextView.getText().toString();
                    recipeListAdapter.clearAllList();
                    loadRecipeList("recipes", search_content);


                }
                return false;
            }
        });

        mEditText.clearFocus();

        mAuth = FirebaseAuth.getInstance();
        loginAndFetchRecipeList();


    }
}
