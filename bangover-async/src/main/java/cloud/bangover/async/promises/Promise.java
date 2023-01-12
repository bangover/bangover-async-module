package cloud.bangover.async.promises;

/**
 * This interface describes the contract, promise abstraction. Promise is the
 * abstraction which can be resolved or rejected. Imagine the situation if your
 * method has to receive response asynchronously for example for long time
 * operations and you don't want waste your CPU resource on response awaiting.
 * In this situation you can return promise, which is resolved on successful
 * complete and rejected on failures. Your client code could subscribe to
 * resolve and reject operations and assign corresponding behavior on each of
 * them situation.
 *
 * @author Dmitry Mikhaylenko
 *
 * @param <T> The promise resolution type
 */
public interface Promise<T> {
	/**
	 * Determine reaction on the promise resolving.
	 *
	 * @param responseHandler The respons handler
	 * @return The derived promise instance
	 */
	public Promise<T> then(ResponseHandler<T> responseHandler);

	/**
	 * Delegate promise resolution affect to the external deferred operation.
	 *
	 * @param resolver The external deferred operation resolver
	 * @return The derived promise instance
	 */
	public Promise<T> then(Deferred<T> resolver);

	/**
	 * Determine reaction on the promise rejection for a specified exception type
	 * and it's subtypes.
	 *
	 * @param <E>          The exception type name
	 * @param errorType    The error type
	 * @param errorHandler THe error handler
	 * @return The derived promise instance
	 */
	public <E extends Throwable> Promise<T> error(Class<E> errorType, ErrorHandler<E> errorHandler);

	/**
	 * Determine default reaction for errors, which isn't matched to the registered
	 * event types and it's subtypes.
	 *
	 * @param errorHandler The error handler
	 * @return The derived promise instance
	 */
	public Promise<T> error(ErrorHandler<Throwable> errorHandler);

	/**
	 * Delegate the promise rejection affect to the external deferred operation.
	 *
	 * @param rejector The external deferred operation
	 * @return The derived promise instance
	 */
	public Promise<T> error(Deferred<T> rejector);

	/**
	 * Chain promise resolution logic using {@link ChainingDeferredFunction}
	 * mechanism.
	 *
	 * @param <C>                      The derived promise resolution type
	 * @param chainingDeferredFunction The chaining deferred function
	 * @return The derived promise instance
	 */
	public <C> Promise<C> chain(ChainingDeferredFunction<T, C> chainingDeferredFunction);

	/**
	 * Chain promise resolution logic using {@link ChainingPromiseHandler}
	 * mechanism.
	 *
	 * @param <C>                     The derived promise resolution type
	 * @param chainingPromiseProvider The derived promise chaining provider function
	 * @return The derived promise instance
	 */
	public <C> Promise<C> chain(ChainingPromiseHandler<T, C> chainingPromiseProvider);

	/**
	 * Delegate resolution or rejection affect to the external deferred operation.
	 *
	 * @param deferred The external deferred operation
	 * @return The derived promise instance
	 */
	public Promise<T> delegate(Deferred<T> deferred);

	/**
	 * Determine the reaction on promise state resolution. It happens after both,
	 * resolve and reject.
	 *
	 * @param finalizer The promise finalizer
	 * @return The derived promise instance
	 */
	public Promise<T> finalize(FinalizingHandler finalizer);

	/**
	 * Wait promise resolution and return value synchronously.
	 *
	 * @param timeout The promise response waiting timeout in seconds
	 * @return The returned value, received on promise resolving
	 * @throws Throwable The thrown exception, received on promise rejection
	 */
	public T get(long timeout) throws Throwable;

	/**
	 * Await promise resolution or rejection.
	 *
	 * @param timeout The timeout
	 * @throws Exception The thrown exception, received on promise rejection 
	 */
	public void await(long timeout) throws Exception;
	
	/**
	 * Await promise resolution or rejection.
	 *
	 * @throws Exception The thrown exception, received on promise rejection 
	 */
	default void await() throws Exception {
		this.await(10);
	}

	/**
	 * This interface declares the contract for the component which handles result
	 * on resolving.
	 *
	 * @author Dmitry Mikhaylenko
	 *
	 * @param <T> The promise result type name.
	 */
	public interface ResponseHandler<T> {
		/**
		 * Receive response asynchronously.
		 *
		 * @param response The response object
		 */
		public void onResponse(T response);
	}

	/**
	 * This interface declares the contract for the component which handles error on
	 * rejecting.
	 *
	 * @author Dmitry Mikhaylenko
	 *
	 * @param <E> The error handler
	 */
	public interface ErrorHandler<E extends Throwable> {
		/**
		 * Receive error asynchronously.
		 *
		 * @param error The error object
		 */
		public void onError(E error);
	}

	/**
	 * This interface the contract for the component which handles notifications
	 * about promise completing(it doesn't matter either promise was resolved or it
	 * was rejected).
	 *
	 * @author Dmitry Mikhaylenko
	 *
	 */
	public interface FinalizingHandler {
		public void onComplete();
	}

	/**
	 * This interface declares the contract for the component which performs a
	 * intermediate deferred operation on promise chaining.
	 *
	 * @author Dmitry Mikhaylenko
	 *
	 * @param <Q> The promise input request type
	 * @param <S> The promise output response resolving type
	 */
	public interface ChainingDeferredFunction<Q, S> {
		/**
		 * Execute intermediate operation.
		 *
		 * @param previousResult The previous promise result
		 * @param deferred       The deferred object
		 */
		public void execute(Q previousResult, Deferred<S> deferred);
	}

	/**
	 * This interface declares the contract for alternative way of response handling
	 * on a promise chaining.
	 *
	 * @author Dmitry Mikhaylenko
	 *
	 * @param <Q> The promise input request type
	 * @param <S> The promise output response resolving type
	 */
	public interface ChainingPromiseHandler<Q, S> {
		/**
		 * Execute intermediate operation.
		 *
		 * @param previousResult The previous promise result
		 * @return The derived promise object
		 */
		public Promise<S> derivePromise(Q previousResult);
	}
}