package com.example.ph;

import javafx.application.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import javafx.stage.*;
import javafx.event.*;
import javafx.geometry.*;
import java.util.*;
import java.io.*;
import javafx.scene.paint.*;
import java.lang.Math;

public class HelloApplication extends Application {

    //**********************************************
//Global variables
//**********************************************
//Default values for size of scene (window).
//Edit as applies.
    public static final int SCENE_WIDTH = 400;
    public static final int SCENE_HEIGHT = 400;

    private ImageView imgView;
    private WritableImage img;
    private StackPane pane;

    //eye, view of the user
    private double[] eye = {0, 0, 1000};

    //Ambient - diffuse - specular variables
    private double[] ka = {0.4, 0.3, 0.4};
    private double[] kd = {0.3, 0.9, 0.4};
    private double[] ks = {0.1, 0.1, 0.8};

    private int specPow = 3;
    //light values
    private double[] lightCol = {0.9, 0.0, 0.2};
    private double[] lightPos = {600, -100, 500};

//*********************************************
//*********************************************

    @Override
    public void start(Stage primaryStage) {
        //Insert code for a JavaFX program here.
//---------------------------------------------------
        img = new WritableImage(SCENE_WIDTH, SCENE_HEIGHT);
        PixelWriter pixelWrite = img.getPixelWriter();

        //----------initializing all the beginning variables --------------
        //sphere coefficients
        double centerX = SCENE_WIDTH/2.0;
        double centerY = SCENE_HEIGHT/2.0;
        double radius = Math.min(SCENE_WIDTH, SCENE_HEIGHT)/3.0;

        for(int xCoord = -SCENE_WIDTH; xCoord < SCENE_WIDTH; xCoord++ ) {
            for(int yCoord = -SCENE_HEIGHT; yCoord < SCENE_HEIGHT; yCoord++ ) {

                //making the generalized coordinates
                double x, y, dist, z;
                x = xCoord;
                y = yCoord;
                dist = Math.sqrt(sqr(x) + sqr(y));
                if(dist <= radius) {
                    z = Math.sqrt(sqr(radius) - sqr(dist));


                    double[] center = {centerX, centerY, 0};
                    double[] point = {x + centerX, y + centerY, z};
                    //computing all the vectors:
                    //normal, viewer, light
                    double[] normal = betweenUV(center, point);
                    double[] viewer = betweenUV(point, eye);

                    double[] light = betweenUV(point, lightPos);

                    //compute the reflection vector
                    double[] reflection = uv(reflectVect(light, normal));
                    //compute individual formulae
                    double[] ambRGB = ambient(ka, lightCol);
                    double[] diffRGB = diffuse(kd, light, normal, lightCol);

                    double[] specRGB = specular(ks, reflection, viewer, lightCol, specPow);

                    double[] illumination = new double[3];

                    illumination = arrSum(new double[][]{ambRGB, diffRGB, specRGB});


                    pixelWrite.setColor((int)(x + centerX), (int)(y + centerY),
                            new Color(illumination[0],
                                    illumination[1],
                                    illumination[2], 1.0));
                }

            }
        }


//---------------------------------------------------


        //Create some subclass of Pane to later be binded to scene
        imgView = new ImageView(img);
        pane = new StackPane();
        pane.getChildren().add(imgView);
        //Code to create a scene and set up "stage" for the target
        //JavaFX window. Uncomment for usage, after editing / / text
        //to function.
        Scene scene = new Scene(pane, SCENE_WIDTH, SCENE_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private double sqr(double x) {return Math.pow(x, 2);}
    //computes the dot product of two 3d vectors
    private double dot(double[] vect1, double[] vect2) {
        double prod = 0;
        for(int i = 0; i < 3; i++ ) {
            prod += vect1[i]*vect2[i];
        }
        return prod;
    }
    //computes and returns the unit vector version of a vector
    private double[] uv(double[] vect) {
        double[] spit = new double[3];
        double vectMag = Math.sqrt(Math.pow(vect[0], 2) +
                Math.pow(vect[1], 2) +
                Math.pow(vect[2], 2));

        for( int i = 0; i < 3; i++ ) {
            spit[i] = vect[i]/vectMag;
        }
        return spit;
    }
    //computes and returns the unit vector between two vectors
    private double[] betweenUV(double[] vectStart, double[] vectEnd) {
        double[] spit = new double[3];
        for(int i = 0; i < 3; i++ ) {
            spit[i] = vectEnd[i] - vectStart[i];
        }
        return uv(spit);
    }

    //Computes the array sum of double arrays with length
//of three
    private double[] arrSum(double[][] arrays) {
        double[] spit = new double[3];
        for(int i = 0; i < 3; i++ ) {
            for(int j = 0; j < arrays.length; j++ ) {
                spit[i] += arrays[j][i];
            }
        }
        return spit;
    }

    //applies ambient equation
    private double[] ambient(double[] ka, double[] lightCol) {
        double[] spit = new double[3];
        for(int i = 0; i < 3; i++ ) {
            spit[i] = ka[i]*lightCol[i];
        }
        return spit;
    }
    //applies diffuse equation
    private double[] diffuse(double[] kd, double[] lightVect, double[] normalVect,
                             double[] lightCol) {
        double[] spit = new double[3];
        double diffuseDot = Math.max((float)0.0, dot(lightVect, normalVect));
        for(int i = 0; i < 3; i++ ) {

            spit[i] = kd[i]*lightCol[i]*diffuseDot;
        }
        return spit;
    }
    //calculates reflection vector
    private double[] reflectVect(double[] lightVect, double[] normalVect) {
        double[] spit = new double[3];
        double lDotN = dot(lightVect, normalVect);
        for(int i = 0; i < 3; i++ ) {
            spit[i] = (2*lDotN*normalVect[i]) - lightVect[i];
        }
        return spit;
    }
    //applies specular equation
    private double[] specular(double[] ks, double[] reflect, double[] vis,
                              double[] lightCol, double pow) {
        double[] spit = new double[3];
        for(int i = 0; i < 3; i++ ) {
            spit[i] = Math.pow(Math.max((float)0.0, dot(reflect, vis)), pow)*ks[i]*lightCol[i];
        }
        return spit;
    }
    private void printArray(String arrayName, double[] arr) {
        for(int i = 0; i < arr.length; i++) {
            System.out.print(arrayName + "[" + i + "]:" + arr[i] + "\t");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        launch();
    }
}


/*
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
*/