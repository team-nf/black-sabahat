// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import frc.robot.subsystems.DriveBase;

//import com.pathplanner.lib.PathConstraints;
//import com.pathplanner.lib.PathPlanner;
//import com.pathplanner.lib.PathPlannerTrajectory;

import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj2.command.Commands;

public final class Autos {
  /** Example static factory for an autonomous command. */
  public static CommandBase exampleAuto(DriveBase subsystem) {
    return Commands.sequence();
  }

  //public static List<PathPlannerTrajectory> simpleCenter = PathPlanner.loadPathGroup("simple path", new PathConstraints(SPEED, PID.MAX_ACC));
  //public static List<PathPlannerTrajectory> chargedCenter = PathPlanner.loadPathGroup("simple + charged", new PathConstraints(SPEED, PID.MAX_ACC));
  //public static List<PathPlannerTrajectory> simpleTop = PathPlanner.loadPathGroup("simple top", new PathConstraints(SPEED, PID.MAX_ACC));
  //public static List<PathPlannerTrajectory> chargedTop = PathPlanner.loadPathGroup("charge top", new PathConstraints(SPEED, PID.MAX_ACC));

  private Autos() {
    throw new UnsupportedOperationException("This is a utility class!");
  }
}
