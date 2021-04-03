package com.example.multithread;

import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;


public class Tool {

    /**
     * 将字符串写入到文本文件中
     */
    public void writeTxtToFile(String strcontent, String filePath,
                               String fileName) {
        // 生成文件夹之后，再生成文件，不然会出错
        makeFilePath(filePath, fileName);// 生成文件

        String strFilePath = filePath + fileName;
        // 每次写入时，都换行写
        String strContent = strcontent + "\r\n";
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                Log.d("TestFile", "Create the file:" + strFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("error:", e + "");
        }
    }

    /**
     * 生成文件
     */
    public File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);// 生成文件夹
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * 生成文件夹
     */
    public static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            Log.i("error:", e + "");
        }
    }

    public String readTxt(String file) {
        Log.e("LZ:", file);
        BufferedReader bre = null;
        String str = "";
        String returnstr = "";
        String a;
        try {

            bre = new BufferedReader(new FileReader(file));//此时获取到的bre就是整个文件的缓存流
            while ((str = bre.readLine()) != null) { // 判断最后一行不存在，为空结束循环

                Log.e("LZ", "readTxt: a------------" + str);

                String[] arr = str.split("\\s+");
                for (String ss : arr) {
                    a = arr[0];
                }

                Log.e("LZ-----str:", str);
                returnstr=str;
            }

        } catch (Exception e) {
            Log.e("LZ", "readTxt: ---------------" + e.toString());
        }
        return returnstr;
    }



    public double[] ReadTxtFile(String strFilePath)
    {
        int index = 0;
        double[] readin_data = new double[240000];
        String path = strFilePath;
        ArrayList<Double> newList = new ArrayList<Double>();
        //open file
        File file = new File(path);
        //如果path是传递过来的参数，可以做一个非目录的判断
        if (file.isDirectory())
        {
            Log.d("TestFile", "The File doesn't exist.");
        }
        else
        {
            try {
                InputStream instream = new FileInputStream(file);
                if (instream != null)
                {
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line;
                    //read by line
                    while (( line = buffreader.readLine()) != null) {
                        newList.add(Double.valueOf(line+"\n"));
                        readin_data[index] = newList.get(index);
                        index++;
                    }
                    instream.close();
                }
            }
            catch (java.io.FileNotFoundException e)
            {
                Log.d("TestFile", "The File doesn't not exist.");
            }
            catch (IOException e)
            {
                Log.d("TestFile", e.getMessage());
            }
        }
        //Log.d("size of list",String.valueOf(newList.size()));
        /*
        for (int i = 0; i < 10; i ++){
            Log.d("the data in list is",String.valueOf(newList.get(i)));
            Log.d("the data is:", String.valueOf(readin_data[i]));
        }
         */

        return readin_data;
    }

}

