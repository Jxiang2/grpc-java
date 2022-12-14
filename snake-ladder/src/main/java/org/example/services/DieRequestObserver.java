package org.example.services;

import io.grpc.stub.StreamObserver;
import java.util.concurrent.ThreadLocalRandom;
import org.example.SnakeLadderRule;
import org.example.models.Die;
import org.example.models.GameState;
import org.example.models.Player;

public class DieRequestObserver implements StreamObserver<Die> {

  private Player client;
  private Player server;
  final private StreamObserver<GameState> gameStateResponseObserver;

  public DieRequestObserver(
      Player client,
      Player server,
      StreamObserver<GameState> responseObserver
  ) {
    this.client = client;
    this.server = server;
    this.gameStateResponseObserver = responseObserver;
  }

  @Override
  public void onNext(Die die) {
    client = getNewPlayerPosition(client, die.getValue());
    if (client.getPosition() != 100) {
      // server roll his dice and move position
      server = getNewPlayerPosition(server, ThreadLocalRandom.current().nextInt(1, 7));
    }

    gameStateResponseObserver.onNext(getGameState());
  }

  @Override
  public void onError(Throwable throwable) {}

  @Override
  public void onCompleted() {
    gameStateResponseObserver.onCompleted();
  }

  private GameState getGameState() {
    return GameState.newBuilder()
        .addPlayer(client)
        .addPlayer(server)
        .build();
  }

  private Player getNewPlayerPosition(Player player, int dieVal) {
    // generate new position by throwing die and applying snake ladder rule
    int newPosition = player.getPosition() + dieVal;
    newPosition = SnakeLadderRule.getPosition(newPosition);

    if (newPosition <= 100) {
      player = player.toBuilder()
          .setPosition(newPosition)
          .build();
    }

    return player;
  }
}
