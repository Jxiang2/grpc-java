package org.example.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.example.models.Balance;
import org.example.models.BalanceCheckRequest;
import org.example.models.BankServiceGrpc;
import org.example.models.BankServiceGrpc.BankServiceBlockingStub;
import org.example.models.BankServiceGrpc.BankServiceStub;
import org.example.models.DepositRequest;
import org.example.models.Money;
import org.example.models.WithdrawRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

@TestInstance(Lifecycle.PER_CLASS)
public class BankClientTest {

  private BankServiceBlockingStub bankServiceBlockingStub;
  private BankServiceStub bankServiceStub;

  @BeforeAll
  public void setup() {
    // server listener
    final ManagedChannel channel = ManagedChannelBuilder
        .forAddress("localhost", 6565)
        .usePlaintext()
        .build();

    // blocking BankService stub
    this.bankServiceBlockingStub = BankServiceGrpc.newBlockingStub(channel);

    // async BankService stub
    this.bankServiceStub = BankServiceGrpc.newStub(channel);
  }

  @Test
  public void balanceTest() {
    final BalanceCheckRequest balanceCheckRequest = BalanceCheckRequest.newBuilder()
        .setAccountNumber(3)
        .build();

    final Balance balance = this.bankServiceBlockingStub.getBalance(balanceCheckRequest);
    System.out.println(balance);
  }

  @Test
  public void withdrawTest() {
    final WithdrawRequest withdrawReq = WithdrawRequest.newBuilder()
        .setAccountNumber(4)
        .setAmount(40)
        .build();

    final Iterator<Money> moneyIterator = this.bankServiceBlockingStub
        .withDraw(withdrawReq);

    try {
      moneyIterator.forEachRemaining(
          money -> System.out.println("received: " + money.getValue())
      );
      System.out.println("done!");
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

  @Test
  public void withdrawAsyncTest() throws InterruptedException {
    CountDownLatch countDownLatch = new CountDownLatch(1); // for test

    final WithdrawRequest withdrawReq = WithdrawRequest.newBuilder()
        .setAccountNumber(10)
        .setAmount(50)
        .build();

    bankServiceStub.withDraw(
        withdrawReq, // request to send
        new MoneyStreamRes(countDownLatch) // response receiver, receiving Money object(s)
    );

    countDownLatch.await(); // for test
  }

  @Test
  public void depositAsyncTest() throws InterruptedException {
    CountDownLatch countDownLatch = new CountDownLatch(1); // for test

    // setup a request stream
    final StreamObserver<DepositRequest> requestStreamObserver =
        bankServiceStub.deposit(new BalanceResponse(countDownLatch));

    // deposit 10 times, each time 10 dollars
    for (int i = 0; i < 10; i++) {
      final DepositRequest deposit =
          DepositRequest.newBuilder().setAccountNumber(8).setAmount(10).build();
      requestStreamObserver.onNext(deposit);
    }

    requestStreamObserver.onCompleted();

    countDownLatch.await(); // for test
  }
}
