package com.kirti.handwriting2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button selectImage;
    Button getResult;
    ImageView imageView;
    Bitmap bitmap;
    private int REQUEST_CODE=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        selectImage=(Button)findViewById(R.id.selectImage);
        getResult=(Button)findViewById(R.id.processImage);
        imageView=(ImageView)findViewById(R.id.imgView);
        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent();

                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Load Picture"),REQUEST_CODE);
            }
        });
        getResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,Main2Activity.class);
                startActivity(intent);


            }
        });
    }

    public Bitmap toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }
    public Bitmap toBinary(Bitmap bmpOriginal) {
        int width, height, threshold;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();
        threshold = 127;
        Bitmap Binary = Bitmap.createBitmap(bmpOriginal);

        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {

                int pixel = bmpOriginal.getPixel(x, y);
                int red = Color.red(pixel);


                if(red < threshold){
                    Binary.setPixel(x, y, 0xFF000000);
                } else{
                    Binary.setPixel(x, y, 0xFFFFFFFF);
                }

            }
        }
        return Binary;
    }
    public Bitmap directional_feature(Bitmap binary){
        int[][] imageData = new int[binary.getHeight()][binary.getWidth()];
        for (int y = 0; y < imageData.length; y++) {
            for (int x = 0; x < imageData[y].length; x++) {
                if (binary.getPixel(x, y) == Color.BLACK) {
                    imageData[y][x] = 1;                 } else {
                    imageData[y][x] = 0;                 }}}
        doZhangSuenThinning(imageData);
        for(int y=1;y<imageData.length;y++){
            for (int x = 1; x < binary.getWidth() - 1; x++){
                if (x <binary.getWidth() && y < binary.getHeight()){
                    if (imageData[y][x] == 1){
                        if (x < binary.getWidth() && y < binary.getHeight()){
                            if (imageData[y - 1][x] == 1 && imageData[y + 1][x] == 1){
                                binary.setPixel(x, y, Color.BLUE);
                            }
                            else if (imageData[y][x - 1] == 1 && imageData[y][x + 1] == 1){
                                if (x < binary.getWidth()) {
                                    binary.setPixel(x, y, Color.RED);
                            }
                        }
                    }
                }

            }
                else{
                    binary.setPixel(x, y, Color.WHITE);
                }
            }
            }


        return binary;
    }

    private int[][] doZhangSuenThinning(final int[][] givenImage) {
        int[][] binaryImageArray;
        binaryImageArray = givenImage;
        int a, b;
        List<Point> pointsToChange = new LinkedList();
        boolean hasChange;
        do{
            hasChange = false;
            for (int y = 1; y + 1 < binaryImageArray.length; y++){
                for (int x = 1; x + 1 < binaryImageArray[y].length; x++){
                    a = getA(binaryImageArray, y, x);      
                    b = getB(binaryImageArray, y, x);
                    if (binaryImageArray[y][x] == 1 && 2 <= b && b <= 6 && a == 1 && (binaryImageArray[y - 1][x] * binaryImageArray[y][x +  1] * binaryImageArray[y + 1][x] == 0)&& (binaryImageArray[y][x + 1] * binaryImageArray[y + 1][x] * binaryImageArray[y][x - 1] == 0)){
                        pointsToChange.add(new Point(x, y));
                        hasChange = true;
                    }
                }
            }
            for (Point point : pointsToChange)

                binaryImageArray[point.y][point.x] = 0;
            pointsToChange.clear();
        }while (hasChange);

            return binaryImageArray;
    }

    private int getB(int[][] binaryImageArray, int y, int x) {
        int count = 0;

        if (y - 1 >= 0 && x + 1 < binaryImageArray[y].length && binaryImageArray[y - 1][x] == 0 && binaryImageArray[y - 1][x + 1] == 1) {
            count++;}
        if (y + 1 < binaryImageArray.length && x - 1 >= 0 && binaryImageArray[y + 1][x - 1] == 0 && binaryImageArray[y][x - 1] == 1) {
            count++;         }
        if (y - 1 >= 0 && x - 1 >= 0 && binaryImageArray[y][x - 1] == 0 && binaryImageArray[y - 1][x - 1] == 1) {
            count++;         }
        if (y - 1 >= 0 && x - 1 >= 0 && binaryImageArray[y - 1][x - 1] == 0 && binaryImageArray[y - 1][x] == 1) {
            count++;         }
        return count;
    }

    private int getA(int[][] binaryImageArray, int y, int x) {
        return binaryImageArray[y - 1][x] + binaryImageArray[y - 1][x + 1] + binaryImageArray[y][x + 1]+ binaryImageArray[y + 1][x + 1] + binaryImageArray[y + 1][x] + binaryImageArray[y + 1][x - 1]+ binaryImageArray[y][x - 1] + binaryImageArray[y - 1][x - 1];
    }
    Bitmap curvature_image,contours;

    public Bitmap curvature_feature(Bitmap image){
        contours= find_contour(image); 
        curvature_image=chunks(contours);
        return curvature_image;
    }

    private Bitmap find_contour(Bitmap image) {
        int[][] imageData = new int[image.getHeight()][ image.getWidth()];
        int[][] contours = new int[ image.getHeight()][ image.getWidth()];
        for (int y = 0; y < imageData.length; y++){
            for (int x = 0; x < imageData[y].length; x++){
                if ( image.getPixel(x, y) == Color.BLACK){
                    imageData[y][x] = 1;
                }
                else{
                    imageData[y][x] = 0;
                }
            }
        }
        return image;
    }

    private Bitmap chunks(Bitmap image) {
        int rows_index = 0;
        int columns_index = 0;
        int black_pixels = 0, white_pixels = 0;
        int[][] chunks;
        int img_height=image.getHeight();
        int img_width=image.getWidth();
        imageData = new int[image.getHeight()][image.getWidth()];
        int c_index = 0,r_index=0,b_ary_rows_index=0,b_ary_col_index=0, i = 0, j = 0;
        for (int y = 0; y < imageData.length; y++) {
            for (int x = 0; x < imageData[y].length; x++){
                if ( image.getPixel(x, y) == Color.BLACK) {
                    imageData[y][x] = 1;
                }
                else if(image.getPixel(x,y)==Color.RED){
                    imageData[y][x]=3;
                }
                else {
                    imageData[y][x] = 0;
                }
            }
        }
        List group_of_arrays = new ArrayList();
        do{
            chunks = new int[5][5];
            do{
                rows_index = r_index;
                columns_index = c_index;
                for (int k = 0; k < 5; k++){
                    if (rows_index < image.getHeight()-1){
                        columns_index=c_index;
                        for (int l = 0; l < 5; l++){
                            if (columns_index  < image.getWidth()-1){
                                chunks[k][l] = imageData[rows_index][columns_index];
                            }
                            columns_index++;
                        }
                        rows_index++;
                    }
                }
            }while (rows_index < r_index + 5 && columns_index<c_index+5 ) ;
            group_of_arrays.add(chunks);
            c_index = c_index + 5;
            if(r_index<img_height){
                if(c_index>img_width){
                    r_index = r_index + 5;
                    c_index=0;
                }
            }
        }while (r_index<img_height && c_index<img_width);
        int[][]chunk;
        c_index=0;
        r_index=0;
        do {
            for (int array_list_index = 0; array_list_index < group_of_arrays.size(); array_list_index++) {
                chunk = new int[5][5];
                chunk = (int[][]) group_of_arrays.get(array_list_index);
                black_pixels = 0;
                white_pixels = 0;
                for (int k = 0; k < 5; k++){
                    for (int l = 0; l < 5; l++){
                        if (chunk[k][l] == 1 || chunk[k][l] == 3){
                            black_pixels++;

                        }
                        else{
                            white_pixels++;
                        }
                    }
                }
                if (white_pixels < black_pixels){
                    for (int m = 0; m < 5; m++){
                        for (int n = 0; n < 5; n++){
                            if (chunk[m][n] == 1){
                                chunk[m][n] = 4;
                            }
                        }
                    }
                }
                else {
                    for (int m = 0; m < 5; m++){
                        for (int n = 0; n < 5; n++){
                            if (chunk[m][n] == 1){
                                chunk[m][n] = 5;
                            }
                        }
                    }
                }
                chunks = new int[5][5];
            }
            do{
                b_ary_rows_index = r_index;
                b_ary_col_index = c_index;
                for (int k = 0; k < 5; k++){
                    if (b_ary_rows_index < image.getHeight() - 1){
                        b_ary_col_index = c_index;
                        for (int l = 0; l < 5; l++) {
                            if (b_ary_col_index < image.getWidth() - 1){
                                if (chunks[k][l] == 4 || chunks[k][l] == 5){
                                    imageData[b_ary_rows_index][b_ary_col_index] = chunks[k][l];
                                }
                            }
                            b_ary_col_index++;
                        }
                        b_ary_rows_index++;
                    }
                }
            }while (b_ary_rows_index < r_index + 5 && b_ary_col_index < c_index + 5);
            c_index = c_index + 5;
            if (r_index < img_height){
                if (c_index > img_width){
                    r_index = r_index + 5;
                    c_index = 0;
                }
            }
        }while (r_index<img_height && c_index<img_width);
        for (int y = 0; y < image.getHeight(); y++){
            for (int x = 0; x < image.getWidth(); x++){
                if( imageData[y][x] == 1){
                    image.setPixel(x, y, Color.BLACK);
                }
                else if( imageData[y][x] == 3){
                    image.setPixel(x, y, Color.BLACK);
                }
                else if (imageData[y][x] == 4){
                    image.setPixel(x,y,Color.RED);
                }
                else if (imageData[y][x] == 5){
                    image.setPixel(x,y,Color.BLUE);
                }
                else{
                    image.setPixel(x, y, Color.WHITE);
                }
            }
        }
        return image;
    }
    int[][] imageData;
    int [][] horizontal_data;
    int[][]vertical_data;
    int [][]slope_data;
    int [][]r_slope_data;
    int[][] index_array;
    int count=0;
    int [] horizontal_index;
    List longest;
    public Bitmap tortuosity_feature(Bitmap image){
        imageData = new int[image.getHeight()][image.getWidth()];
        index_array=new int[image.getHeight()][image.getWidth()];
        horizontal_index=new int[image.getHeight()];
        slope_data=new int[image.getHeight()][image.getWidth()];
        r_slope_data=new int[image.getHeight()][image.getWidth()];
        horizontal_data=new int[image.getHeight()][image.getWidth()];
        vertical_data=new int[image.getHeight()][image.getWidth()];
        longest=new ArrayList();
        for (int y = 0; y < imageData.length; y++) {
            for (int x = 0; x < imageData[y].length-1; x++) {
                index_array[y][x] = count;
                if (image.getPixel(x, y) == Color.BLACK) {
                    imageData[y][x] = 1;
                }
                else {
                    imageData[y][x] = 0;
                }
                count++;
            }
            }
        for (int y = 0; y < image.getHeight()-1; y++){
            for (int x = 0; x < image.getWidth()-1; x++){
                slope_data[y][x]=imageData[y][x];
                r_slope_data[y][x]=imageData[y][x];
                horizontal_data[y][x]=imageData[y][x];
                vertical_data[y][x]=imageData[y][x];
            }
        }
        for(int rows=1;rows<image.getHeight()-1;rows++)     {
            for(int columns=1;columns<image.getWidth()-1;columns++) {
                if (rows < image.getHeight() - 1 && columns < image.getWidth() - 1) {
                    if (horizontal_data[rows][columns] == 1 || horizontal_data[rows][columns - 1] == 1 && horizontal_data[rows][columns + 1] == 1){
                        horizontal_data[rows][columns] = 7;
                        horizontal_data[rows][columns+1]=7;
                    }
                }
            }
            }
        ArrayList horizontal_lines = new ArrayList();
        int[] h_line;
        int row=1,column=1,loop=0;
        do{
            column=1;
            do {
                if (column < image.getWidth() - 1) {
                    if (horizontal_data[row][column] == 7) {
                        loop=0;
                        h_line = new int[image.getWidth()];
                        do{
                            h_line[loop]=index_array[row][column];
                            column++;
                            loop++;
                        }while (horizontal_data[row][column] == 7 && column<image.getWidth()-1);
                        horizontal_lines.add(h_line);
                    }
                    else{
                        column++;
                    }
                }

            }while(column<image.getWidth()-1);
            row++;
        }while(row<image.getHeight()-1);
        int[] temp_array;int size=0;
        for (int array_list_index = 0; array_list_index < horizontal_lines.size(); array_list_index++){
            temp_array = new int[image.getWidth()];
            temp_array=(int[]) horizontal_lines.get(array_list_index);
            size=0;
            do{
                size++;
            }while (temp_array[size] != 0);
            longest.add(size);
        }
        int index=0,maximum=0,temp=0;
        for (int array_list_index = 0; array_list_index < longest.size(); array_list_index++){
            if(array_list_index==0){
                maximum=(int)longest.get(array_list_index);
                index=array_list_index;
            }
            else if(array_list_index>0){
                temp=(int)longest.get(array_list_index);
                if(maximum<temp){
                    maximum=temp;
                    index=array_list_index;
                }
            }

}
        int loop_variable=0;
        int[] largest;
        largest = (int[]) horizontal_lines.get(index);
        for(int rows=1;rows<image.getHeight()-1 && loop_variable<image.getWidth()-1;rows++){
            for(int columns=1;columns<image.getWidth()-1 && loop_variable<image.getWidth()-1;columns++){
                if(index_array[rows][columns]==largest[loop_variable]){
                    horizontal_data[rows][columns]=2;
                    loop_variable++;
                }
                else if(horizontal_data[rows][columns]==7){
                    horizontal_data[rows][columns]=1;
                }
            }
        }
        for (int rows = 0; rows < image.getWidth() - 1; rows++){
            for (int columns = 1; columns < image.getHeight() - 1; columns++){
                if (rows < image.getWidth() - 1 && columns < image.getHeight()){
                    if (vertical_data[columns][rows] == 1 ||vertical_data[columns - 1][rows]  == 1 && vertical_data[columns + 1][rows] == 1){
                        vertical_data[columns - 1][rows] = 5;
                        vertical_data[columns][rows] = 5;
                    }
                }
            }

        }
        List vertical_lines=new ArrayList();
        int[]v_line;
        row=0;
        column=1;
        do{
            column=1;
            do {
                if (column < image.getHeight() - 1) {
                    if (vertical_data[column][row] == 5) {
                        loop = 0;
                        v_line = new int[image.getHeight()];
                        do {
                            v_line[loop] = index_array[column][row];
                            column++;
                            loop++;
                        } while (vertical_data[column][row] == 5 && column < image.getHeight() - 1);
                        vertical_lines.add(v_line);
                    } else
                        column++;
                }
            }while(column<image.getHeight()-1);
            row++;
        }while(row<image.getWidth()-1);
        int[] v_temp_array;int v_size=0;
        List v_longest=new ArrayList();
        for (int array_list_index = 0; array_list_index<vertical_lines.size(); array_list_index++) {
            v_temp_array = new int[image.getWidth()];
            v_temp_array = (int[]) vertical_lines.get(array_list_index);
            v_size = 0;
            do {
                v_size++;
            } while (v_temp_array[v_size] != 0);
            v_longest.add(v_size);
            index = 0;
            maximum = 0;
            temp = 0;
            for (array_list_index = 0; array_list_index < v_longest.size(); array_list_index++) {
                if (array_list_index == 0) {
                    maximum = (int) v_longest.get(array_list_index);
                    index = array_list_index;
                } else if (array_list_index > 0) {
                    temp = (int) v_longest.get(array_list_index);
                    if (maximum < temp) {
                        maximum = temp;
                        index = array_list_index;
                    }
                }
            }
        }
            int[] v_largest=new int[image.getHeight()];
            v_largest = (int[]) vertical_lines.get(index);
            loop_variable=0;
            for(int rows=0;rows<image.getWidth()-1 && loop_variable<image.getHeight()-1;rows++){
                for(int columns=1;columns<image.getHeight()-1 && loop_variable<image.getHeight()-1;columns++){
                    if(index_array[columns][rows]==v_largest[loop_variable]){
                        vertical_data[columns][rows]=3;
                        loop_variable++;
                    }
                    else if(vertical_data[columns][rows]==5 || vertical_data[columns-1][rows]==5){
                        vertical_data[columns][rows]=1;
                        vertical_data[columns-1][rows]=1;
                    }
                }
            }
            int counter=2;
            for (int rows = 1; rows < image.getWidth() - 1; rows++){
                for (int columns = 1; columns < image.getHeight() - 1; columns++){
                    if ((slope_data[columns + 1][rows + 1] == 1 || slope_data[columns-1][rows-1]==1 ||slope_data[columns-1][rows-1]>1) && slope_data[columns][rows] == 1){
                        if(slope_data[columns-1][rows-1]>1) {
                            slope_data[columns][rows] = slope_data[columns - 1][rows - 1];
                        }
                        else if(slope_data[columns-1][rows-1]==1){
                            slope_data[columns][rows] = counter;
                            slope_data[columns - 1][rows - 1] = counter;
                            counter++;
                        }
                    }
                }
            }
            List s_line=new ArrayList();
            List line_index=new ArrayList();
            int temp_count;
            do{
                temp_count=0;
                for (int i=1;i<image.getHeight()-1;i++){
                    for(int j=1;j<image.getWidth();j++){
                        if (slope_data[i][j]==counter){
                            temp_count++;
                        }
                    }
                }
                s_line.add(temp_count);
                line_index.add(counter);
                counter--;
            }while(counter>1);
            int value=0,max=(int)s_line.get(0);
            for(int k=1;k<image.getHeight()-1;k++){
                if((int)s_line.get(k)>max){
                    max=(int)s_line.get(k);
                    value=(int) line_index.get(k);
                }
            }
            for (int rows = 1; rows < image.getHeight() - 1; rows++){
                for (int columns = 1; columns < image.getWidth() - 1; columns++) {

                    if (slope_data[rows][column] == value) {
                        slope_data[rows][column] = 4;
                    } else
                        slope_data[rows][column] = 0;

                }
            }
            counter=2;
            for (int rows = 1; rows <image.getHeight()-1; rows++){
                for (int columns = 1; columns <image.getWidth()-1; columns++){
                    if ((r_slope_data[rows+1][columns - 1] == 1 ||r_slope_data[rows-1][columns+1]>1) && r_slope_data[rows][columns] == 1){
                        if(r_slope_data[rows-1][columns+1]>1){
                            r_slope_data[rows][columns] =r_slope_data[rows-1][columns+1];
                        }
                        else if(r_slope_data[rows+1][columns - 1]==1){
                            r_slope_data[rows][columns] = counter;
                            r_slope_data[rows+1][columns - 1] = counter;
                            counter++;
                        }
                    }
                }
            }
            List r_s_line=new ArrayList();     List r_line_index=new ArrayList();     int r_temp_count;
            do{
                r_temp_count=0;
                for (int i=1;i<image.getHeight()-1;i++){
                    for(int j=1;j<image.getWidth();j++){
                        if (r_slope_data[i][j]==counter){
                            r_temp_count++;
                        }
                    }
                }
                r_s_line.add(r_temp_count);
                r_line_index.add(counter);
                counter--;
            }while(counter>1);
            value=0;
            max=(int)r_s_line.get(0);
            for(int k=1;k<image.getHeight()-1;k++){
                if((int)r_s_line.get(k)>max){
                    max=(int)r_s_line.get(k);
                    value=(int) r_line_index.get(k);
                }
            }
            for (int rows = 1; rows < image.getHeight() - 1; rows++){
                for (int columns = 1; columns < image.getWidth() - 1; columns++){
                    if(horizontal_data[rows][columns]==2) {
                        imageData[rows][columns]=4;
                    }
                    else if(vertical_data[rows][columns]==3) {
                        imageData[rows][columns]=4;
                    }
                    else if(slope_data[rows][columns]==4)
                    {
                        imageData[rows][columns]=4;
                    }
                    else if (r_slope_data[rows][columns]==value)
                    {
                        imageData[rows][columns]=4;
                    }
                }
            }
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++){
                    if (imageData[y][x] == 1 ) {
                        image.setPixel(x, y, Color.BLUE);
                    }
                    else if (imageData[y][x] == 4) {
                        image.setPixel(x, y, Color.RED);
                    }
                    else {
                        image.setPixel(x, y, Color.WHITE);
                    }
                }
            }

        return image;

    }


    @Override
    protected  void onActivityResult(int requestCode,int resultCode,Intent data)
    {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==REQUEST_CODE&& resultCode==RESULT_OK && data!=null && data.getData()!=null)
        {

            Uri uri=data.getData();
            try {

               Bitmap bitmap= MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                imageView.setImageBitmap(bitmap);


            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
