package cloud.bangover.async.promises;

import cloud.bangover.async.promises.Promises.PromiseRejectionDuplicateException;
import cloud.bangover.async.promises.Promises.PromiseResolutionDuplicateException;
import cloud.bangover.async.timer.Timeout;
import cloud.bangover.async.timer.Timer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class PromisesTest {
  private static final String RESPONSE = "HELLO WORLD!";
  private static final RuntimeException TYPED_EXCEPTION = new RuntimeException("THE TYPED ERROR!");
  private static final Exception UNTYPED_EXCEPTION = new Exception("THE UNTYPED ERROR!");

  @Test
  public void shouldResolvePromisesWithoutErrors() throws Exception {
    // Given
    MockResponseHandler<String> firstResponseHandler = new MockResponseHandler<String>();
    MockResponseHandler<String> secondResponseHandler = new MockResponseHandler<String>();
    MockErrorHandler<RuntimeException> firstErrorHandler = new MockErrorHandler<RuntimeException>();
    MockErrorHandler<Throwable> secondErrorHandler = new MockErrorHandler<Throwable>();
    // When
    Promises.of(new Deferred.DeferredFunction<String>() {
      @Override
      public void execute(Deferred<String> deferred) {
        Timer.sleep(1000L);
        deferred.resolve(RESPONSE);
      }
    }).then(firstResponseHandler).then(secondResponseHandler)
        .error(RuntimeException.class, firstErrorHandler).error(secondErrorHandler).await();
    // Then
    
    Assert.assertTrue(firstResponseHandler.getHistory().hasEntry(0, RESPONSE));
    Assert.assertTrue(secondResponseHandler.getHistory().hasEntry(0, RESPONSE));
    Assert.assertFalse(firstErrorHandler.getHistory().hasEntries());
    Assert.assertFalse(secondErrorHandler.getHistory().hasEntries());
  }

  @Test
  public void shouldTypedErrorHandlerBeResolved() throws Exception {
    // Given
    MockResponseHandler<String> responseHandler = new MockResponseHandler<String>();
    MockErrorHandler<RuntimeException> firstErrorHandler = new MockErrorHandler<RuntimeException>();
    MockErrorHandler<Throwable> secondErrorHandler = new MockErrorHandler<Throwable>();
    // When
    Promises.of(new Deferred.DeferredFunction<String>() {
      @Override
      public void execute(Deferred<String> deferred) {
        Timer.sleep(1000L);
        deferred.reject(TYPED_EXCEPTION);
      }
    }).then(responseHandler).error(RuntimeException.class, firstErrorHandler)
        .error(secondErrorHandler).await();
    // Then
    Assert.assertFalse(responseHandler.getHistory().hasEntries());
    Assert.assertTrue(firstErrorHandler.getHistory().hasEntry(0, TYPED_EXCEPTION));
    Assert.assertFalse(secondErrorHandler.getHistory().hasEntries());
  }

  @Test
  public void shouldUntypedErrorHandlerBeResolved() throws Exception {
    // Given
    MockResponseHandler<String> responseHandler = new MockResponseHandler<String>();
    MockErrorHandler<RuntimeException> firstErrorHandler = new MockErrorHandler<RuntimeException>();
    MockErrorHandler<Throwable> secondErrorHandler = new MockErrorHandler<Throwable>();
    // When
    Promises.of(new Deferred.DeferredFunction<String>() {
      @Override
      public void execute(Deferred<String> deferred) {
        Timer.sleep(1000L);
        deferred.reject(UNTYPED_EXCEPTION);
      }
    }).then(responseHandler).error(RuntimeException.class, firstErrorHandler)
        .error(secondErrorHandler).await();
    // Then
    Assert.assertFalse(responseHandler.getHistory().hasEntries());
    Assert.assertFalse(firstErrorHandler.getHistory().hasEntries());
    Assert.assertTrue(secondErrorHandler.getHistory().hasEntry(0, UNTYPED_EXCEPTION));
  }

  @Test
  public void shouldPromiseBeRejectedIfExceptionIsThrownFromDeferredFunction() throws Exception {
    // Given
    MockResponseHandler<String> responseHandler = new MockResponseHandler<String>();
    MockErrorHandler<Throwable> errorHandler = new MockErrorHandler<Throwable>();
    // When
    Promises.of(new Deferred.DeferredFunction<String>() {
      @Override
      public void execute(Deferred<String> deferred) {
        Timer.sleep(1000L);
        throw TYPED_EXCEPTION;
      }
    }).then(responseHandler).error(errorHandler).await();
    // Then
    Assert.assertFalse(responseHandler.getHistory().hasEntries());
    Assert.assertTrue(errorHandler.getHistory().hasEntry(0, TYPED_EXCEPTION));
  }

  @Test
  public void shouldPromiseBeProtectedOfDoubleResolution() throws Exception {
    // Given
    MockErrorHandler<PromiseResolutionDuplicateException> manualHandler =
        new MockErrorHandler<PromiseResolutionDuplicateException>();
    MockErrorHandler<PromiseResolutionDuplicateException> errorHandler =
        new MockErrorHandler<PromiseResolutionDuplicateException>();
    // When
    Promises.of(new Deferred.DeferredFunction<String>() {
      @Override
      public void execute(Deferred<String> deferred) {
        try {
          deferred.resolve(RESPONSE);
          deferred.resolve(RESPONSE);
        } catch (PromiseResolutionDuplicateException error) {
          manualHandler.onError(error);
        }
      }
    }).error(PromiseResolutionDuplicateException.class, errorHandler);
    Timer.sleep(Timeout.ofSeconds(10L));
    
    // Then
    Assert.assertTrue(manualHandler.getHistory().hasEntries());
    Assert.assertFalse(errorHandler.getHistory().hasEntries());
  }

  @Test
  public void shouldPromiseBeProtectedOfDoubleRejection() throws Exception {
    // Given
    MockErrorHandler<PromiseRejectionDuplicateException> manualHandler =
        new MockErrorHandler<PromiseRejectionDuplicateException>();
    MockErrorHandler<PromiseRejectionDuplicateException> errorHandler =
        new MockErrorHandler<PromiseRejectionDuplicateException>();
    // When
    Promises.of(new Deferred.DeferredFunction<String>() {
      @Override
      public void execute(Deferred<String> deferred) {
        try {
          deferred.reject(TYPED_EXCEPTION);
          deferred.reject(TYPED_EXCEPTION);
        } catch (PromiseRejectionDuplicateException error) {
          manualHandler.onError(error);
        }
      }
    }).error(PromiseRejectionDuplicateException.class, errorHandler);
    Timer.sleep(Timeout.ofSeconds(10L));
    
    // Then
    Assert.assertTrue(manualHandler.getHistory().hasEntries());
    Assert.assertFalse(errorHandler.getHistory().hasEntries());
  }

  @Test
  public void shouldPromiseBeProtectedOfRejectionAfterResolution() throws Exception {
    // Given
    MockErrorHandler<PromiseResolutionDuplicateException> manualHandler =
        new MockErrorHandler<PromiseResolutionDuplicateException>();
    MockErrorHandler<PromiseResolutionDuplicateException> errorHandler =
        new MockErrorHandler<PromiseResolutionDuplicateException>();
    // When
    Promises.of(new Deferred.DeferredFunction<String>() {
      @Override
      public void execute(Deferred<String> deferred) {
        try {
          deferred.resolve(RESPONSE);
          deferred.reject(TYPED_EXCEPTION);
        } catch (PromiseResolutionDuplicateException error) {
          manualHandler.onError(error);
        }
      }
    }).error(PromiseResolutionDuplicateException.class, errorHandler);
    Timer.sleep(Timeout.ofSeconds(10L));
    
    // Then
    Assert.assertTrue(manualHandler.getHistory().hasEntries());
    Assert.assertFalse(errorHandler.getHistory().hasEntries());
  }

  @Test
  public void shouldPromiseBeProtectedOfResolutionAfterRejection() throws Exception {
    // Given
    MockErrorHandler<PromiseRejectionDuplicateException> manualHandler =
        new MockErrorHandler<PromiseRejectionDuplicateException>();
    MockErrorHandler<PromiseRejectionDuplicateException> errorHandler =
        new MockErrorHandler<PromiseRejectionDuplicateException>();
    // When
    Promises.of(new Deferred.DeferredFunction<String>() {
      @Override
      public void execute(Deferred<String> deferred) {
        try {
          deferred.reject(TYPED_EXCEPTION);
          deferred.resolve(RESPONSE);
        } catch (PromiseRejectionDuplicateException error) {
          manualHandler.onError(error);
        }
      }
    }).error(PromiseRejectionDuplicateException.class, errorHandler).await();
    // Then
    Assert.assertTrue(manualHandler.getHistory().hasEntries());
    Assert.assertFalse(errorHandler.getHistory().hasEntries());
  }

  @Test
  public void shouldResolvePromisesChain() throws Exception {
    // Given
    MockResponseHandler<Integer> initialResponseHandler = new MockResponseHandler<Integer>();
    MockResponseHandler<String> chainedResponseHandler = new MockResponseHandler<String>();
    // When
    Promises.of(new Deferred.DeferredFunction<Integer>() {
      @Override
      public void execute(Deferred<Integer> deferred) {
        deferred.resolve(100);
      }
    }).then(initialResponseHandler).chain(new Promise.ChainingDeferredFunction<Integer, String>() {
      @Override
      public void execute(Integer previousResult, Deferred<String> deferred) {
        deferred.resolve(previousResult.toString());
      }
    }).then(chainedResponseHandler).await();
    // Then
    Assert.assertTrue(initialResponseHandler.getHistory().hasEntry(0, 100));
    Assert.assertTrue(chainedResponseHandler.getHistory().hasEntry(0, "100"));
  }

  @Test
  public void shouldRejectChainedPromiseInCaseWhenInitialResolvedButChainedRejected() throws Exception {
    // Given
    MockResponseHandler<Integer> initialResponseHandler = new MockResponseHandler<Integer>();
    MockResponseHandler<String> chainedResponseHandler = new MockResponseHandler<String>();
    MockErrorHandler<RuntimeException> chainedErrorHandler =
        new MockErrorHandler<RuntimeException>();
    // When
    Promises.of(new Deferred.DeferredFunction<Integer>() {
      @Override
      public void execute(Deferred<Integer> deferred) {
        deferred.resolve(100);
      }
    }).then(initialResponseHandler).chain(new Promise.ChainingDeferredFunction<Integer, String>() {
      @Override
      public void execute(Integer previousResult, Deferred<String> deferred) {
        deferred.reject(TYPED_EXCEPTION);
      }
    }).then(chainedResponseHandler).error(RuntimeException.class, chainedErrorHandler).await();
    // Then
    Assert.assertTrue(initialResponseHandler.getHistory().hasEntry(0, 100));
    Assert.assertFalse(chainedResponseHandler.getHistory().hasEntries());
    Assert.assertTrue(chainedErrorHandler.getHistory().hasEntry(0, TYPED_EXCEPTION));
  }

  @Test
  public void shouldRejectChainedPromiseInCaseWhenInitialResolvedButExceptionThrownInChainedDeferredFunction() throws Exception {
    // Given
    MockResponseHandler<Integer> initialResponseHandler = new MockResponseHandler<Integer>();
    MockResponseHandler<String> chainedResponseHandler = new MockResponseHandler<String>();
    MockErrorHandler<RuntimeException> chainedErrorHandler =
        new MockErrorHandler<RuntimeException>();
    // When
    Promises.of(new Deferred.DeferredFunction<Integer>() {
      @Override
      public void execute(Deferred<Integer> deferred) {
        deferred.resolve(100);
      }
    }).then(initialResponseHandler).chain(new Promise.ChainingDeferredFunction<Integer, String>() {
      @Override
      public void execute(Integer previousResult, Deferred<String> deferred) {
        throw TYPED_EXCEPTION;
      }
    }).then(chainedResponseHandler).error(RuntimeException.class, chainedErrorHandler).await();
    // Then
    Assert.assertTrue(initialResponseHandler.getHistory().hasEntry(0, 100));
    Assert.assertFalse(chainedResponseHandler.getHistory().hasEntries());
    Assert.assertTrue(chainedErrorHandler.getHistory().hasEntry(0, TYPED_EXCEPTION));
  }

  @Test
  public void shouldResolvedPromiseBeCreatedByValue() throws Exception {
    // Given
    MockResponseHandler<String> responseHandler = new MockResponseHandler<String>();
    // When
    Promises.resolvedBy(RESPONSE).then(responseHandler).await();
    // Then
    Assert.assertTrue(responseHandler.getHistory().hasEntry(0, RESPONSE));
  }
  
  @Test
  public void shouldRejectedPromiseBeCreatedByError() throws Exception {
    // Given
    MockErrorHandler<RuntimeException> errorHandler =
        new MockErrorHandler<RuntimeException>();
    // When
    Promises.rejectedBy(TYPED_EXCEPTION).error(RuntimeException.class, errorHandler).await();
    // Then
    Assert.assertTrue(errorHandler.getHistory().hasEntry(0, TYPED_EXCEPTION));    
  }
  
  @Test
  public void shouldPromiseBeFinalizedOnResolve() throws Exception {
    // Given
    MockFinalizingHandler finalizingHandler = new MockFinalizingHandler();
    // When
    Promises.resolvedBy(RESPONSE).finalize(finalizingHandler).await();
    // Then
    Assert.assertTrue(finalizingHandler.isFinalized());
  }
  
  @Test
  public void shouldPromiseBeFinalizedOnReject() throws Exception {
    // Given
    MockFinalizingHandler finalizingHandler = new MockFinalizingHandler();
    // When
    Promises.rejectedBy(TYPED_EXCEPTION).finalize(finalizingHandler).await();
    // Then
    Assert.assertTrue(finalizingHandler.isFinalized());
  }
  
  @Test
  public void shouldResultBeReturnedSynchronously() throws Throwable {
    Assert.assertEquals(RESPONSE, Promises.resolvedBy(RESPONSE).get(1L));
  }
  
  @Test(expected = RuntimeException.class)
  public void shouldErrorBeRethrownOnRejection() throws Throwable {
    Promises.rejectedBy(TYPED_EXCEPTION).get(1L);
  }
}
