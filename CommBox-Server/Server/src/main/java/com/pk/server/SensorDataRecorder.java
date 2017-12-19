package com.pk.server;

import com.pk.common.Constants;
import com.pk.common.RotationalSensorData;
import com.pk.common.SensorData;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;

public class SensorDataRecorder extends Thread {
    int i = 0;
    List<SensorData> preAcc = new ArrayList<>();
    List<SensorData> postAcc = new ArrayList<>();
    List<SensorData> preGyro = new ArrayList<>();
    List<SensorData> postGyro = new ArrayList<>();
    private Socket currentSocket;

    public SensorDataRecorder(Socket socket) {
        currentSocket = socket;
    }

    private SensorData getSensorData(DataInputStream in) throws IOException {
        float x = in.readFloat();
        float y = in.readFloat();
        float z = in.readFloat();
        long ts = in.readLong();
        return new SensorData(x, y, z, ts);
    }
    public void run() {

            while (true) {
                DataInputStream in = null;
//                try {
//                    in = new DataInputStream(currentSocket.getInputStream());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                preAcc.clear();
                postAcc.clear();
                preGyro.clear();
                postGyro.clear();

//                try {
//                    double level = in.readDouble();
//                    System.out.println(level);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                try {
                    in = new DataInputStream(currentSocket.getInputStream());
                    int val = in.readInt();

                    if (val == -1) {
                        in.close();
                        break;
                    } else {
                        int preAccCount = val;
                        for (int i = 0; i < preAccCount; i++) {
                            preAcc.add(getSensorData(in));
                        }

                        int postAccCount = in.readInt();
                        for (int i = 0; i < postAccCount; i++) {
                            postAcc.add(getSensorData(in));
                        }

                        int preGyroCount = in.readInt();
                        for (int i = 0; i < preGyroCount; i++) {
                            preGyro.add(getSensorData(in));
                        }

                        int postGyroCount = in.readInt();
                        for (int i = 0; i < postGyroCount; i++) {
                            postGyro.add(getSensorData(in));
                        }

                        writeOut();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("I am Here Error");
                    break;
                }
            }

//        writeOut();

    }

//    public void writeOut2() {
//        System.out.println("Pre Acc");
//        for (SensorData sensorData : preAcc) {
//            System.out.println(sensorData.toString());
//        }
//
//        System.out.println("Post Acc");
//        for (SensorData sensorData : postAcc) {
//            System.out.println(sensorData.toString());
//        }
//
//        System.out.println("Pre Gyro");
//        for (SensorData sensorData : preGyro) {
//            System.out.println(sensorData.toString());
//        }
//
//        System.out.println("Post Gyro");
//        for (SensorData sensorData : postGyro) {
//            System.out.println(sensorData.toString());
//        }
//    }

    private RotationalSensorData getRotationVectorFromGyro(SensorData sensorData,
                                                           float timeFactor)
    {
        float[] normValues = new float[3];
        float[] gyroValues = new float[3];
        float[] deltaRotationVector =  new float[4];

        gyroValues[0] = sensorData.get_x();
        gyroValues[1] = sensorData.get_y();
        gyroValues[2] = sensorData.get_z();

        // Calculate the angular speed of the sample
        float omegaMagnitude =
                (float)Math.sqrt(gyroValues[0] * gyroValues[0] +
                        gyroValues[1] * gyroValues[1] +
                        gyroValues[2] * gyroValues[2]);

        // Normalize the rotation vector if it's big enough to get the axi
        if(omegaMagnitude > Constants.EPSILON) {
            normValues[0] = gyroValues[0] / omegaMagnitude;
            normValues[1] = gyroValues[1] / omegaMagnitude;
            normValues[2] = gyroValues[2] / omegaMagnitude;
        }

        // Integrate around this axis with the angular speed by the timestep
        // in order to get a delta rotation from this sample over the timestep
        // We will convert this axis-angle representation of the delta rotation
        // into a quaternion before turning it into the rotation matrix.
        float thetaOverTwo = omegaMagnitude * timeFactor;
        float sinThetaOverTwo = (float)Math.sin(thetaOverTwo);
        float cosThetaOverTwo = (float)Math.cos(thetaOverTwo);
        deltaRotationVector[0] = sinThetaOverTwo * normValues[0];
        deltaRotationVector[1] = sinThetaOverTwo * normValues[1];
        deltaRotationVector[2] = sinThetaOverTwo * normValues[2];
        deltaRotationVector[3] = cosThetaOverTwo;

        return new RotationalSensorData(deltaRotationVector[0], deltaRotationVector[1], deltaRotationVector[2] , deltaRotationVector[3], -1L);
    }

    float GYROSCOPE_SENSITIVITY = 65.6f;

    float M_PI = 3.14159265359f;

    float dt =  0.01f;						// 10 ms sample rate!


    public class CompValues {
        public float pitch;
        public float roll;
        public CompValues(float p, float r){
            pitch = p;
            roll = r;
        }

        public float getPitch() {
            return pitch;
        }

        public void setPitch(float pitch) {
            this.pitch = pitch;
        }

        public float getRoll() {
            return roll;
        }

        public void setRoll(float roll) {
            this.roll = roll;
        }

        @Override
        public String toString() {
            return new StringBuilder("[").append(pitch).append(", ").append(roll).append("]").toString();
        }
    }

    public CompValues ComplementaryFilter(SensorData accSensorData, SensorData gyroSensorData , float pitch, float roll)
    {
        float pitchAcc, rollAcc;
        Float[] accData = new Float[3];
        Float[] gyrData = new Float[3];

        accData[0] = accSensorData.get_x();
        accData[1] = accSensorData.get_y();
        accData[2] = accSensorData.get_z();

        gyrData[0] = gyroSensorData.get_x();
        gyrData[1] = gyroSensorData.get_y();
        gyrData[2] = gyroSensorData.get_z();

        // Integrate the gyroscope data -> int(angularSpeed) = angle
        pitch += ((float)gyrData[0] / GYROSCOPE_SENSITIVITY) * dt; // Angle around the X-axis
        roll -= ((float)gyrData[1] / GYROSCOPE_SENSITIVITY) * dt;    // Angle around the Y-axis

        // Compensate for drift with accelerometer data if !bullshit
        // Sensitivity = -16 to  16 G at 16Bit -> 16G = 262144 && 0.5G = 8192
        int forceMagnitudeApprox = (int) (abs(accData[0]) + abs(accData[1]) + abs(accData[2]));
        if (forceMagnitudeApprox > 1024 && forceMagnitudeApprox < 262144)
        {
            // Turning around the X axis results in a vector on the Y-axis
            pitchAcc = (float) (Math.atan2((float)accData[1], (float)accData[2]) * 180 / M_PI);
            pitch = (float) (pitch * 0.98 + pitchAcc * 0.02);

            // Turning around the Y axis results in a vector on the X-axis
            rollAcc = (float) (Math.atan2((float)accData[0], (float)accData[2]) * 180 / M_PI);
            roll = (float) (roll * 0.98 + rollAcc * 0.02);
        }

        return new CompValues(pitch, roll);
    }


    public void writeOut() {
        i+=1;
        System.out.println(i+ " Entry Recorded !!");

        List<RotationalSensorData> preRotationalSensorDataList = new ArrayList<>();

        long preT = preGyro.get(0).get_timeStamp();
        long postT = 0;
        for (int i =1; i< preGyro.size(); i++) {
            postT = preGyro.get(i).get_timeStamp();
            preRotationalSensorDataList.add(getRotationVectorFromGyro(preGyro.get(i), (postT-preT)/2f));
            preT = postT;
        }

        List<RotationalSensorData> postRotationalSensorDataList = new ArrayList<>();

        preT = postGyro.get(0).get_timeStamp();
        postT = 0;
        for (int i =1; i< postGyro.size(); i++) {
            postT = postGyro.get(i).get_timeStamp();
            postRotationalSensorDataList.add(getRotationVectorFromGyro(postGyro.get(i), (postT-preT)/2f));
            preT = postT;
        }

        int accSize = preAcc.size();
        int gyroSize = preGyro.size();

        int min = accSize;
        if (gyroSize < accSize) {
            min = gyroSize;
        }

        List<CompValues> preCompValues = new ArrayList<>();
        float prePitch = 0;
        float preRoll = 0;

        for (int i =0; i< min; i++) {

            CompValues compValues = ComplementaryFilter(preAcc.get(i), preGyro.get(i), prePitch, preRoll);
            preCompValues.add(compValues);

            prePitch = compValues.getPitch();
            preRoll = compValues.getRoll();
        }



        accSize = postAcc.size();
        gyroSize = postGyro.size();

        min = accSize;
        if (gyroSize < accSize) {
            min = gyroSize;
        }

        List<CompValues> postCompValues = new ArrayList<>();
        prePitch = 0;
        preRoll = 0;

        for (int i =0; i< min; i++) {

            CompValues compValues = ComplementaryFilter(postAcc.get(i), postGyro.get(i), prePitch, preRoll);
            postCompValues.add(compValues);

            prePitch = compValues.getPitch();
            preRoll = compValues.getRoll();
        }



        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("Classifier/ML-Model-Copy/Test/test_vectors_gyro_pre.txt", false)));
//            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("Classifier/ML-Model/Train/test_vectors_gyro_pre.txt", true)));
            out.println(preGyro.toString());
            out.close();
        } catch (IOException e) {
            //exception handling left as an exercise for the reader
        }

        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("Classifier/ML-Model-Copy/Test/test_vectors_gyro_post.txt", false)));
//            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("Classifier/ML-Model/Train/test_vectors_gyro_post.txt", true)));
            out.println(postGyro.toString());
            out.close();
        } catch (IOException e) {
            //exception handling left as an exercise for the reader
        }

        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("Classifier/ML-Model-Copy/Test/test_vectors_acc_pre.txt", false)));
//            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("Classifier/ML-Model/Train/test_vectors_acc_pre.txt", true)));
            out.println(preAcc.toString());
            out.close();
        } catch (IOException e) {
            //exception handling left as an exercise for the reader
        }

        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("Classifier/ML-Model-Copy/Test/test_vectors_acc_post.txt", false)));
//            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("Classifier/ML-Model/Train/test_vectors_acc_post.txt", true)));
            out.println(postAcc.toString());
            out.close();
        } catch (IOException e) {
            //exception handling left as an exercise for the reader
        }

        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("Classifier/ML-Model-Copy/Test/test_vectors_rot_pre.txt", false)));
//            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("Classifier/ML-Model/Train/test_vectors_rot_pre.txt", true)));
            out.println(preRotationalSensorDataList.toString());
            out.close();
        } catch (IOException e) {
            // exception handling left as an exercise for the reader
        }

        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("Classifier/ML-Model-Copy/Test/test_vectors_rot_post.txt", false)));
//            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("Classifier/ML-Model/Train/test_vectors_rot_post.txt", true)));
            out.println(postRotationalSensorDataList.toString());
            out.close();
        } catch (IOException e) {
            //exception handling left as an exercise for the reader
        }

        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("Classifier/ML-Model-Copy/Test/test_vectors_comp_pre.txt", false)));
//            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("Classifier/ML-Model/Train/test_vectors_comp_pre.txt", true)));
            out.println(preCompValues.toString());
            out.close();
        } catch (IOException e) {
            //exception handling left as an exercise for the reader
        }

        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("Classifier/ML-Model-Copy/Test/test_vectors_comp_post.txt", false)));
//            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("Classifier/ML-Model/Train/test_vectors_comp_post.txt", true)));
            out.println(postCompValues.toString());
            out.close();
        } catch (IOException e) {
            //exception handling left as an exercise for the reader
        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            Runtime.getRuntime().exec("python ./Classifier/ML-Model-Copy/test.py").waitFor();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try (BufferedReader br = new BufferedReader(new FileReader("./Classifier/ML-Model-Copy/Result/result.txt")))
        {

            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                System.out.println(sCurrentLine);

                if ("sweep".equals(sCurrentLine)) {
                    sCurrentLine = "cut";
                }
                String IMG_PATH = "C:\\Users\\dell\\Pictures\\CommBox\\";
                ResultDialog dialog = new ResultDialog(new JFrame(), sCurrentLine + " shot",IMG_PATH+sCurrentLine+".jpg");
                // set the size of the window
                dialog.setSize(600, 400);

                int num;
                if (Math.random()<=0.5) {
                    num = 1;
                } else {
                    num = 2;
                }
                String resourcePath ;



                if ("Cut".equals(sCurrentLine)) {

                    resourcePath = "cut/cut_" + num + ".mp3";
                } else if ("Straight".equals(sCurrentLine)) {


                    resourcePath = "pull/pull_" + num + ".mp3";
                } else if ("Pull".equals(sCurrentLine)){


                    resourcePath = "straight/st_" + num + ".mp3";
                }
            }



        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
