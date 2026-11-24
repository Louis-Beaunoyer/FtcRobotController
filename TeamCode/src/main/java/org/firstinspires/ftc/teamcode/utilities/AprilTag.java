package org.firstinspires.ftc.teamcode.utilities;

import com.qualcomm.hardware.dfrobot.HuskyLens;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AprilTag {

    protected HuskyLens huskyLens;
    protected HuskyLens.Block[] blockList;
    protected HashMap<Integer, HuskyLens.Block> idBlockDict;
    protected ArrayList<Integer> closestTagsId;

    protected enum HorizontalPosition {Left, Center, Right}

    protected enum VerticalPosition {Top, Center, Bottom}
    protected DriveTrain driveTrain;
    private int lockedTagId = -1;
    private Boolean isAligning = false;



    public AprilTag(HuskyLens huskyLensConstructor,DriveTrain driveTrain) {
        this.huskyLens = huskyLensConstructor;
        this.driveTrain = driveTrain;


    }

    public void init() {
        blockList = huskyLens.blocks();
        idBlockDict = makeIdBlockDict();
        closestTagsId = sortTagsByDistance();
    }

    public void loop() {
        blockList = huskyLens.blocks();
        idBlockDict = makeIdBlockDict();
        closestTagsId = sortTagsByDistance();
    }
    public void telemetry(Telemetry telemetry) {
        telemetry.addData("Closest Tags", closestTagsId);
        telemetry.addData("Ids: ", getIds());
        telemetry.addData("Block: ", blockList);
    }

    public List<Integer> getIds() {
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < blockList.length; i++) {
            if (blockList[i] != null) {
                if (isTagIdInAlliance(blockList[i].id)) {
                    ids.add(blockList[i].id);
                } else {
                    continue;
                }

            }
        }
        return ids;
    }

    public HuskyLens.Block getBlocksWithId(int id) {
        HashMap<Integer, HuskyLens.Block> dict = makeIdBlockDict();
        return dict.get(id);
    }

    public HashMap<Integer, HuskyLens.Block> makeIdBlockDict() {
        HashMap<Integer, HuskyLens.Block> dict = new HashMap<>();
        List<Integer> idList = getIds();
        for (int i = 0; i < blockList.length; i++) {
            if (blockList[i] != null) {
                dict.put(blockList[i].id, blockList[i]);
            }
        }
        return dict;
    }

    public double getXWithId(int id) {
        return getBlocksWithId(id).x;
    }

    public double getYWithId(int id) {
        return getBlocksWithId(id).y;
    }

    public double getHeightWithId(int id) {
        return getBlocksWithId(id).height;
    }

    public double getWidthWithId(int id) {
        return getBlocksWithId(id).width;
    }

    public HorizontalPosition getTagHorizontalPositionWithId(int id) {
        double x = getXWithId(id);
        return x < Config.CAMERA_WIDTH / 2 ? HorizontalPosition.Left : x > Config.CAMERA_WIDTH / 2 ? HorizontalPosition.Right : HorizontalPosition.Center;
    }

    public VerticalPosition getTagVerticalPositionWithId(int id) {
        double y = getYWithId(id);
        return y < Config.CAMERA_HEIGHT / 2 ? VerticalPosition.Top : y > Config.CAMERA_HEIGHT / 2 ? VerticalPosition.Bottom : VerticalPosition.Center;
    }

    public int getClosestTagId() {
        ArrayList<Integer> sortedIds = sortTagsByDistance();
        if (sortedIds.isEmpty()) {
            return 17;
        }else{
        return sortedIds.get(0);}
    }

    public ArrayList<Integer> sortTagsByDistance() {
        ArrayList<Integer> sortedIds = new ArrayList<>(getIds());
        sortedIds.sort((id1,id2) -> Double.compare(getWidthWithId(id1),getWidthWithId(id2)));
        return sortedIds;
    }

    public Boolean isTagIdInAlliance(int id) {
        if (Config.
                getIds().contains(id)) {
            return true;
        } else {
            return false;
        }

    }
    public void startsAligningToClosestTag() {
        if (getIds().size() > 0) {
            lockedTagId = getClosestTagId();
            isAligning = true;
        }
    }
    public boolean isAligning() {
        return isAligning;
    }
    public void stopAligning(){
        isAligning = false;
        lockedTagId = -1;
    }
    public void followIdToShootingPosition() {
        if(!isAligning || lockedTagId == -1){
            return;
        }
        double strafePower = 0;
        double forwardPower = 0;
        double wantedX = Config.APRIL_TAG_SHOOTING_POSITION_WANTED_X;
        double toleranceX = Config.APRIL_TAG_SHOOTING_POSITION_X_TOLERANCE;
        double toleranceWidth = Config.APRIL_TAG_SHOOTING_POSITION_WIDTH_TOLERANCE;
        double wantedWidth = Config.APRIL_TAG_SHOOTING_POSITION_WANTED_TAG_WIDTH;
        double drivingSpeed = Config.APRIL_TAG_SHOOTING_POSITION_DRIVING_SPEED;
        HuskyLens.Block chosenBlockToFollow;
        HorizontalPosition horizontalPosition;
        boolean isXCorrect;
        boolean isWidthCorrect;
        chosenBlockToFollow = getBlocksWithId(lockedTagId);
        if (chosenBlockToFollow == null) {
            driveTrain.drive(0,0,0);
            stopAligning();
            return;
        }
        isXCorrect =  Math.abs(wantedX - chosenBlockToFollow.x) < toleranceX;
        isWidthCorrect = Math.abs(wantedWidth - chosenBlockToFollow.width) < toleranceWidth;
        if (!isXCorrect) {
            horizontalPosition = getTagHorizontalPositionWithId(lockedTagId);
            if (horizontalPosition == HorizontalPosition.Left) {
                strafePower = -drivingSpeed;

            }else if (horizontalPosition == HorizontalPosition.Right) {
                strafePower = drivingSpeed;

            }

        }
        if (!isWidthCorrect) {
            if (chosenBlockToFollow.width < wantedWidth) {
                forwardPower = drivingSpeed;
            }else{
                forwardPower = -drivingSpeed;
            }
        }
        if (isXCorrect && isWidthCorrect) {
            driveTrain.drive(0,0,0);
            stopAligning();
        }else{
            driveTrain.drive(forwardPower,strafePower,0);
        }


    }



}



