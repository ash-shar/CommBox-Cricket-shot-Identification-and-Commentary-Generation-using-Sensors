public static final float EPSILON = 0.000000001f;
 
private void getRotationVectorFromGyro(float[] gyroValues,
                                       float[] deltaRotationVector,
                                       float timeFactor)
{
    float[] normValues = new float[3];
 
    // Calculate the angular speed of the sample
    float omegaMagnitude =
        (float)Math.sqrt(gyroValues[0] * gyroValues[0] +
        gyroValues[1] * gyroValues[1] +
        gyroValues[2] * gyroValues[2]);
 
    // Normalize the rotation vector if it's big enough to get the axis
    if(omegaMagnitude > EPSILON) {
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
} 