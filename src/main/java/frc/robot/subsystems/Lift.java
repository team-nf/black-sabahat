// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.SparkMaxLimitSwitch;
import com.revrobotics.SparkMaxPIDController;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.SparkMaxRelativeEncoder.Type;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import static frc.robot.Constants.LiftConstants.*;
import static frc.robot.Constants.TestConstants.*;

public class Lift extends SubsystemBase {
  // Neo
  private final CANSparkMax m_driver = new CANSparkMax(MOTOR_ID, MotorType.kBrushless);
  
  // Encoder
  // counts per revolution is the amount of millimeters the lift moves every revolution
  private final RelativeEncoder m_encoder = m_driver.getEncoder(Type.kHallSensor, (int)(ENCODER_CPR));
  private final SparkMaxPIDController pidcontroller = m_driver.getPIDController();

  // Limit Switches
  private final DigitalInput m_topLimit = new DigitalInput(LIMIT_CH_1);
  private final DigitalInput m_bottomLimit = new DigitalInput(LIMIT_CH_2);

  //private final SparkMaxLimitSwitch m_topLimit = m_driver.getForwardLimitSwitch(com.revrobotics.SparkMaxLimitSwitch.Type.kNormallyClosed);
  //private final SparkMaxLimitSwitch m_bottomLimit = m_driver.getReverseLimitSwitch(com.revrobotics.SparkMaxLimitSwitch.Type.kNormallyClosed);

  /** Creates a new Lift. */
  public Lift() {
    m_encoder.setPositionConversionFactor(DISTANCE_PER_COUNT);
  }

  public CommandBase lift() {
    if (m_topLimit.get()) {return run(() -> m_driver.stopMotor());}
    return startEnd(() -> m_driver.set(0.2), () -> m_driver.stopMotor());
  }

  public CommandBase lower() {
    if (m_bottomLimit.get()) {return run(() -> m_driver.stopMotor());}
    return startEnd(() -> m_driver.set(-0.2), () -> m_driver.stopMotor());
  }

  /**
   * In-line brake command factory
   * (I am thinking of maybe instead of a seperated command i can incorporate this into the lift command)
   */
  public CommandBase brake() {
    return this.runOnce(() -> m_driver.stopMotor());
  }

  /**
   * Condition method.
   * Panic if limit switch is hit.
   * Used for the brake.
   * 
   * @return True for panic, false for OK.
   */
  public boolean panicCondition() {
    if(m_topLimit.get()) {return true;}
    if(m_bottomLimit.get()) {return true;}
    return false;
  }

  /**
   * Condition method
   * 
   * @return True if lift is in motion, false if stationary.
   */
  public boolean motion() {
    if (m_driver.get() != 0) {return true;}
    return false;
  }

  // TESTING
  public CommandBase test() {
    return startEnd(() -> brake(), () -> brake());
  }

  public void testSpeed() {
    m_driver.set(TEST_LIFT_SPEED);
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
  }
}
