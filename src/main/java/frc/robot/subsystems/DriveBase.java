// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import static frc.robot.Constants.DriveBaseConstants.*;
import static frc.robot.Constants.OperatorConstants.*;

import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.DifferentialDriveKinematics;
import edu.wpi.first.math.kinematics.DifferentialDriveOdometry;
import edu.wpi.first.math.kinematics.DifferentialDriveWheelSpeeds;
import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.SPI.Port;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
//import frc.robot.commands.ShiftGear;
import frc.robot.Constants.OperatorConstants;

//import com.github.yehpop.nfsensors.*;


public class DriveBase extends SubsystemBase {
  private static enum Mode {
    SingleJoystickCustom(0),
    SingleJoystickArcade(1),
    DoubleJoystickCustom(2),
    DoubleJoystickArcade(3),
    DoubleJoystickTank(4),
    DoubleJoystickCurve(5);
    private final int value;

    public static Mode set(int value) {
      return Mode.values()[value];
    }
    private Mode(int value) {
      this.value = value;
    }
  }
  private Mode kDriveMode = Mode.set(DRIVE_MODE);
  // Motors
  private final WPI_VictorSPX m_leftMaster = new WPI_VictorSPX(MOTOR_PORT_1);
  private final WPI_VictorSPX m_leftFollower = new WPI_VictorSPX(MOTOR_PORT_2);
  private final WPI_VictorSPX m_leftThird = new WPI_VictorSPX(MOTOR_PORT_3);
  
  private final WPI_VictorSPX m_rightMaster = new WPI_VictorSPX(MOTOR_PORT_4);
  private final WPI_VictorSPX m_rightFollower = new WPI_VictorSPX(MOTOR_PORT_5);
  private final WPI_VictorSPX m_rightThird = new WPI_VictorSPX(MOTOR_PORT_6);
  
  private final MotorControllerGroup m_leftMotors;
  private final MotorControllerGroup m_rightMotors;

  // Control systems for motors
  private final PIDController m_leftPIDController = new PIDController(PID.P, PID.I, PID.D);
  private final PIDController m_rightPIDController = new PIDController(PID.P, PID.I, PID.D);
  
  private final SimpleMotorFeedforward m_feedforward = new SimpleMotorFeedforward(PID.S, PID.V);
  
  // Shifter
  private final DoubleSolenoid m_shifter = new DoubleSolenoid(PN_ID, MODULE_TYPE, FORWARD_CHANNEL, REVERSE_CHANNEL);
  
  // Sensors

  // Switch
  private final DigitalInput m_limit = new DigitalInput(LIMIT_CH);

  // Gyro
  // This gyro is used at autonomous for the horizontal axis
  private final ADXRS450_Gyro m_gyro = new ADXRS450_Gyro(Port.kOnboardCS0);
  // This gyro is used at charge station balance, etc.
  //private final MPU6050 mpu = new MPU6050();
  
  // Encoders
  private final Encoder m_leftEncoder = new Encoder(LEFT_ENCODER_PORT_A, LEFT_ENCODER_PORT_B, LEFT_ENCODER_REVERSED, Encoder.EncodingType.k4X); 
  private final Encoder m_rightEncoder = new Encoder(RIGHT_ENCODER_PORT_A, RIGHT_ENCODER_PORT_B, RIGHT_ENCODER_REVERSED, Encoder.EncodingType.k4X); 

  private final DifferentialDrive m_drive;

  // Odometry and Kinematics
  private final DifferentialDriveOdometry m_hOdometry;
  private final DifferentialDriveKinematics m_kinematics = new DifferentialDriveKinematics(TRACK_WIDTH);

  /**Constructor. Creates new drive base object. */
  public DriveBase() {
    // Define motor driver groups and invert right side.
    m_leftMotors = new MotorControllerGroup(m_leftMaster, m_leftFollower, m_leftThird);
    m_rightMotors = new MotorControllerGroup(m_rightMaster, m_rightFollower, m_rightThird);
    m_leftMotors.setInverted(LEFT_MOTORS_REVERSED);
    m_rightMotors.setInverted(RIGHT_MOTORS_REVERSED);

    m_drive = new DifferentialDrive(m_leftMotors, m_rightMotors);

    // Set encoders
    m_leftEncoder.setDistancePerPulse(DISTANCE_PER_PULSE);
    m_rightEncoder.setDistancePerPulse(DISTANCE_PER_PULSE);

    // Set solenoid
    m_shifter.set(Value.kForward);

    // Construct odometry object
    m_hOdometry = new DifferentialDriveOdometry(m_gyro.getRotation2d(), m_leftEncoder.getDistance(), m_rightEncoder.getDistance());
    OperatorConstants.DRIVER_DASHBOARD.add("left encoder", m_leftEncoder);
    OperatorConstants.DRIVER_DASHBOARD.add("right encoder", m_rightEncoder);
  }

  public void setSpeeds(DifferentialDriveWheelSpeeds speeds, boolean isTest) {
    if (isTest) {
      m_drive.tankDrive(speeds.leftMetersPerSecond / SPEED, speeds.rightMetersPerSecond / SPEED);
    }
    final double leftFeedfoward = m_feedforward.calculate(speeds.leftMetersPerSecond);
    final double rightFeedfoward = m_feedforward.calculate(speeds.rightMetersPerSecond);

    final double leftOutput = m_leftPIDController.calculate(m_leftEncoder.getRate(), speeds.leftMetersPerSecond);
    final double rightOutput = m_rightPIDController.calculate(m_rightEncoder.getRate(), speeds.rightMetersPerSecond);

    m_leftMotors.setVoltage(leftOutput + leftFeedfoward);
    m_rightMotors.setVoltage(rightOutput + rightFeedfoward);
  }
  
  public void setSpeeds(DifferentialDriveWheelSpeeds speeds) {
    final double leftFeedfoward = m_feedforward.calculate(speeds.leftMetersPerSecond);
    final double rightFeedfoward = m_feedforward.calculate(speeds.rightMetersPerSecond);

    final double leftOutput = m_leftPIDController.calculate(m_leftEncoder.getRate(), speeds.leftMetersPerSecond);
    final double rightOutput = m_rightPIDController.calculate(m_rightEncoder.getRate(), speeds.rightMetersPerSecond);

    m_leftMotors.setVoltage(leftOutput + leftFeedfoward);
    m_rightMotors.setVoltage(rightOutput + rightFeedfoward);
  }

  public void drive(double speed, double rotation) {
    var wheelSpeeds = m_kinematics.toWheelSpeeds(new ChassisSpeeds(speed, 0.0, rotation));
    setSpeeds(wheelSpeeds);
  }

  public void drive(double speed, double rotation, boolean isTest) {
    var wheelSpeeds = m_kinematics.toWheelSpeeds(new ChassisSpeeds(speed, 0.0, rotation));
    setSpeeds(wheelSpeeds, isTest);
  }

  /**
   * This is for testing.
   * 
   * I may not be able to apply control systems in such a manner.
   * In such case this shall change.
   * 
   * This may need not be used any so.
   */
  public void arcadeDrive(double speed, double rot) {
    double ff = m_feedforward.calculate(speed);
    final PIDController fwdController = new PIDController(PID.P, PID.I, PID.D);
    final PIDController rotController = new PIDController(PID.P, PID.I, PID.D);
    final double fwdOutput = fwdController.calculate(getEncodersAveragedRate(), speed);
    final double rotOutput = rotController.calculate(m_gyro.getRate(), rot);
    fwdController.close(); // Can i even do this? is this right? maybe just allocate at the beginning i dont know.
    rotController.close();
    
    m_drive.arcadeDrive(fwdOutput / 12.0 + ff, rotOutput / 12.0);
  }

  public void tankDrive(double left, double right) {
    m_drive.tankDrive(left, right);
  }

  public void curveDrive(double speed, double curve) {
    m_drive.curvatureDrive(speed, curve, false);
  }

  public void balance() {
    var pid = new PIDController(0.8, 0, 0);
    double s = pid.calculate(m_gyro.getAngle(), 0);
    pid.close();
    arcadeDrive(s, 0);
  }

  public void setMaxOutput(double maxOutput) {
    m_drive.setMaxOutput(maxOutput);
  }
  
  public void setMode(int mode) {
    kDriveMode = Mode.values()[mode];
  }

  public int getMode() {
    return this.kDriveMode.value;
  }
  
  public void resetEncoders() {
    m_leftEncoder.reset();
    m_rightEncoder.reset();
  }

  public double getEncodersAveraged() {
    return (m_leftEncoder.getDistance() + m_rightEncoder.getDistance()) / 2;
  }

  public double getEncodersAveragedRate() {
    return (m_leftEncoder.getRate() + m_rightEncoder.getRate()) / 2;
  }

  public double getLeftEncoder() {
    return m_leftEncoder.getDistance();
  }

  public double getRightEncoder() {
    return m_rightEncoder.getDistance();
  }

  public Pose2d getPose2d() {
    return m_hOdometry.getPoseMeters();
  } 

  public void resetPose(Pose2d pose) {
    m_hOdometry.resetPosition(m_gyro.getRotation2d(), m_leftEncoder.getDistance(), m_rightEncoder.getDistance(), pose);
  }

  public double getHeading() {
    return m_gyro.getRotation2d().getDegrees();
  }

  public DifferentialDriveKinematics kinematics() {
    return m_kinematics;
  }

  public CommandBase toggleMode() {
    if(kDriveMode.value == 5) {return this.runOnce(() -> kDriveMode = Mode.values()[0]);}
    return this.runOnce(() -> kDriveMode = Mode.values()[kDriveMode.value + 1]);
  }

  public CommandBase changeMode(int mode) {
    return this.runOnce(() -> setMode(mode)); 
  }

  public CommandBase brake() {
    return this.run(() -> {m_leftMotors.stopMotor(); m_rightMotors.stopMotor();});
  }

  public CommandBase leftFwd(double voltage) {
    return this.runEnd(() -> {m_leftMotors.setVoltage(voltage);}, () -> {m_leftMotors.stopMotor();});
  }

  public CommandBase leftRev(double voltage) {
  return this.runEnd(() -> {m_leftMotors.setVoltage(voltage);}, () -> {m_leftMotors.stopMotor();});
  }

  public CommandBase rightFwd(double voltage) {
    return this.runEnd(() -> {m_rightMotors.setVoltage(voltage);}, () -> {m_rightMotors.stopMotor();});
  }

  public CommandBase rightRev(double voltage)  {
    return this.runEnd(() -> {m_rightMotors.setVoltage(voltage);}, () -> {m_rightMotors.stopMotor();});
  }

  /**
   * Command constructer method.
   *
   * @return command instant command that shifts the gears at the drive base.
   */
  public CommandBase shiftGear() {
    // Inline construction of command goes here.
    // Subsystem::RunOnce implicitly requires `this` subsystem.
    return this.runOnce(() -> m_shifter.toggle());
  }

  public CommandBase climb() {
    return this.run(this::balance);
  }
  
  /**
   * An example method querying a boolean state of the subsystem (for example, a digital sensor).
   *
   * @return value of some boolean subsystem state, such as a digital sensor.
   */
  public boolean exampleCondition() {
    // Query some boolean state, such as a digital sensor.
    return false;
  }
  
  /**
   * Condition of motion.
   * False; if both sides of drive base are not in motion
   * 
   * @return boolean value.
   */
  public boolean motion() {
    if (m_rightMotors.get() != 0 & m_leftMotors.get() != 0) {
      return true;
    }
    return false;
  }

  /**
   * Condition of the shifter on the drive base.
   * Used to reset shifter when drive base is not in motion.
   * 
   * @return true if shifter is not pushed.
   */
  public boolean shifterCondition() {
    if (m_shifter.get() == Value.kReverse) {return true;}
    return false;
  }

  // TEST
  public CommandBase test() {
    return startEnd(() -> m_rightEncoder.reset(), () -> m_leftEncoder.reset());
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run

    // Update Odometry Pose
    m_hOdometry.update(m_gyro.getRotation2d(), m_leftEncoder.getDistance(), m_rightEncoder.getDistance());
  }

  @Override
  public void simulationPeriodic() {
    // This method will be called once per scheduler run during simulation
    // For testing purposes
    m_leftMotors.set(0.5);
    m_rightMotors.set(0.2);

    m_hOdometry.update(m_gyro.getRotation2d(), m_leftEncoder.getDistance(), m_rightEncoder.getDistance());

    System.out.println(m_hOdometry.getPoseMeters());
  }
}
