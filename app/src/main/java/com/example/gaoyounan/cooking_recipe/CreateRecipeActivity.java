package com.example.gaoyounan.cooking_recipe;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gaoyounan.cooking_recipe.model.Recipe;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CreateRecipeActivity extends AppCompatActivity {

    private Button back_button;
    private Button add_ingredient_button;
    private Button minus_ingredient_button;

    private Button add_direction_button;
    private Button minus_direction_button;

    private Button chooseImageButton;
    private ImageView recipe_create_picture;

    private Button buttonCreate;

    private EditText recipe_name_edit_text;
    private EditText recipe_description_edit_text;
    private EditText txt_url_edit_text;

    private int ingredients_nums = 0;
    private int direction_nums = 0;
    private Context mContext = null;

    private LinearLayout layoutIngredient;
    private LinearLayout layoutDirection;

    private Uri filePath = null;

    private static final int MAX_NUM = 20;
    private static final int PICK_IMAGE_REQUEST = 71;

    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private Recipe recipeEntity = new Recipe();

    private ProgressDialog progressDialog;



    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private View addEditViewIntoLayout(String textHint)
    {
        View convertView = null;
        convertView = LayoutInflater.from(mContext).inflate(
                R.layout.dynamic_edit_view, null);

        EditText editText = convertView.findViewById(R.id.txt_recipe_dynamic_input);
        editText.setHint(textHint);
        return convertView;
    }

    private void handleDynamic_EditView_Button()
    {
        layoutIngredient = findViewById(R.id.ingredients_input_layout);

        add_ingredient_button = findViewById(R.id.button_add_ingredient);
        add_ingredient_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(ingredients_nums < MAX_NUM)
                {
                    layoutIngredient.addView(addEditViewIntoLayout("Please Input Ingredients!"));
                    ingredients_nums++;
                }

            }
        });

        minus_ingredient_button = findViewById(R.id.button_minus_ingredient);
        minus_ingredient_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(ingredients_nums > 0)
                {
                    layoutIngredient.removeViewAt(ingredients_nums-1);
                    ingredients_nums--;
                }

            }
        });

        layoutDirection = findViewById(R.id.direction_input_layout);

        add_direction_button = findViewById(R.id.button_add_direction);
        add_direction_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(direction_nums < MAX_NUM)
                {
                    layoutDirection.addView(addEditViewIntoLayout("Please Input Direction!"));
                    direction_nums++;
                }

            }
        });

        minus_direction_button = findViewById(R.id.button_minus_direction);
        minus_direction_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(direction_nums > 0)
                {
                    layoutDirection.removeViewAt(direction_nums-1);
                    direction_nums--;
                }

            }
        });
    }

    private void handleFillingDataIntoRecipe()
    {
        recipeEntity.setRecipe_name(recipe_name_edit_text.getText().toString().trim());
        recipeEntity.setDescription(recipe_description_edit_text.getText().toString().trim());

        List<String> ingredient_list = new ArrayList<String>();
        List<String> direction_list = new ArrayList<String>();

        EditText viewTemp = null;
        String str_temp = null;
        for(int i=0; i < ingredients_nums; i++)
        {
            viewTemp = (EditText)layoutIngredient.getChildAt(i);
            str_temp = viewTemp.getText().toString();
            if(str_temp != null && !"".equals(str_temp.trim()))
            {
                ingredient_list.add(str_temp.trim());
            }
        }

        recipeEntity.setIngredients(ingredient_list);

        for(int i=0; i < direction_nums; i++)
        {
            viewTemp = (EditText)layoutDirection.getChildAt(i);
            str_temp = viewTemp.getText().toString();
            if(str_temp != null && !"".equals(str_temp.trim()))
            {
                direction_list.add(str_temp.trim());
            }
        }

        recipeEntity.setDirection(direction_list);

        if(txt_url_edit_text.getText() != null)
        {
            String url_text = txt_url_edit_text.getText().toString();
            recipeEntity.setUrl(url_text.toString());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_recipe);

        mContext = this;
        storageReference = FirebaseStorage.getInstance().getReference();
        progressDialog = new ProgressDialog(this);

        TextView tv_tile = findViewById(R.id.text_title);
        tv_tile.setText("Create Recipe");

        handleDynamic_EditView_Button();
        back_button = findViewById(R.id.button_backward);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(CreateRecipeActivity.this, MainActivity.class);
                startActivity(intent);
                CreateRecipeActivity.this.finish();
            }
        });

        chooseImageButton = findViewById(R.id.chooseImageButton);
        chooseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

        recipe_create_picture = findViewById(R.id.recipe_create_picture);


        recipe_name_edit_text = findViewById(R.id.txt_recipe_name);
        recipe_description_edit_text = findViewById(R.id.txt_recipe_description);
        txt_url_edit_text = findViewById(R.id.txt_url);


        buttonCreate = findViewById(R.id.buttonCreate);
        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(filePath == null)
                {
                    Toast.makeText(CreateRecipeActivity.this, "Recipe Picture is mandatory", Toast.LENGTH_SHORT).show();
                    return;
                }

                String recipe_name = recipe_name_edit_text.getText().toString();

                if(recipe_name == null || "".equals(recipe_name.trim()))
                {
                    Toast.makeText(CreateRecipeActivity.this, "Recipe name could not be blank ", Toast.LENGTH_SHORT).show();
                    return;
                }

                Query myTopPostsQuery = FirebaseDatabase.getInstance().getReference().child("recipes/"+recipe_name);

                ValueEventListener recipeListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get Post object and use the values to update the UI
                        Recipe recipeTemp = dataSnapshot.getValue(Recipe.class);
                        if(recipeTemp!=null)
                        {
                            Toast.makeText(CreateRecipeActivity.this, "Recipe name exits!Please Change another name ", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        else
                        {
                            handleFillingDataIntoRecipe();
                            uploadImageAndCreateRecipe();

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                        Toast.makeText(CreateRecipeActivity.this, "Error:"+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                };

                myTopPostsQuery.addListenerForSingleValueEvent(recipeListener);

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                recipe_create_picture.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void uploadImageAndCreateRecipe() {

        if(filePath != null)
        {
            progressDialog.setTitle("Creating...");
            progressDialog.show();

            String image_id = String.valueOf(System.currentTimeMillis());
            StorageReference ref = storageReference.child("recipe_images/"+ image_id);
            recipeEntity.setImage_path(image_id);

            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(CreateRecipeActivity.this, "Create Recipe Successfully", Toast.LENGTH_SHORT).show();

                            databaseReference = FirebaseDatabase.getInstance().getReference();
                            databaseReference.child("recipes/"+recipeEntity.getRecipe_name()).setValue(recipeEntity);

                            Intent intent = new Intent();
                            intent.setClass(CreateRecipeActivity.this, MainActivity.class);
                            startActivity(intent);
                            CreateRecipeActivity.this.finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(CreateRecipeActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage(""+(int)progress+"%");
                        }
                    });
        }
    }
}
