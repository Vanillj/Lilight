package Managers.Network;

import Components.Entities.PlayerEntity;
import Components.PlayerComponents.B2dBodyComponent;
import Data.FixedValues;
import DataShared.Network.UpdatePackage;
import DataShared.Player.PlayerData;
import Managers.Map.Map;
import Managers.Map.MapLayer;

import Managers.PlayerManager;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.physics.box2d.Body;
import com.vaniljstudio.server.ServerClass;

public class UserIdentity extends Entity {
    public String UniqueID;
    public int connectionID;
    public PlayerEntity entity;
    public PlayerData playerData;
    public MapLayer currentLayer;
    public Map currentMap;
    public String UserName;

    private float timer = 0;

    public UserIdentity(String UniqueID, int connectionID){
        this.UniqueID = UniqueID;
        this.connectionID = connectionID;
    }

    public void RemoveUserIdentity(){
        currentLayer.RemoveUserFromLayer(this);
    }

    public void Update(float delta) {
        timer += delta;
        //MAKE THIS 1/20 later
        if (timer >= FixedValues.UpdateFrequency30)
        {
            //Updates playerData position to the physical body position
            UpdatePlayerPositionData();

            //Creates new package with data
            UpdatePackage UpdatePackage = new UpdatePackage();
            //Sets data
            UpdatePackage.recieverData = playerData;
            FillOtherUsers(UpdatePackage);

            ServerClass.GameServer.getServer().sendToTCP(connectionID, UpdatePackage);
            System.out.println("Sent data to " + connectionID);

            timer -= FixedValues.UpdateFrequency30;
        }

        //TODO SEND INFORMATION TO USER

    }

    private void UpdatePlayerPositionData(){
        Body body = entity.getComponent(B2dBodyComponent.class).body;
        PlayerManager.SetPosition(playerData, body.getPosition());
    }

    private void FillOtherUsers(UpdatePackage UpdatePackage){
        if(currentLayer != null){
            for (UserIdentity userIdentity : currentLayer.users.values()) {
                //TODO CHANGE DISTANCE
                if (PlayerManager.GetPosition(playerData).len() < 2000 && connectionID != userIdentity.connectionID)
                {
                    UpdatePackage.OtherPlayers.add(userIdentity.playerData);
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof UserIdentity)
            return ((UserIdentity) o).connectionID == this.connectionID;
        return false;
    }
}
