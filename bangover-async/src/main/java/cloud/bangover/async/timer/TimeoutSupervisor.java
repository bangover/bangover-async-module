package cloud.bangover.async.timer;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This class supervise that timeout has not been exceeded and if it is happened the
 * {@link TimeoutCallback#onTimeout()} will be called.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public final class TimeoutSupervisor {
  private Optional<ScheduledExecutorService> executorService = Optional.empty();
  private final TimeoutCallback callback;
  private final Timeout timeout;

  /**
   * Create the timeout supervisor.
   *
   * @param timeout  The timeout value
   * @param callback The timeout handling callback.
   */
  TimeoutSupervisor(Timeout timeout, TimeoutCallback callback) {
    super();
    this.callback = callback;
    this.timeout = timeout;
  }

  /**
   * Start the timeout supervision.
   */
  public void startSupervision() {
    this.executorService = Optional.of(Executors.newSingleThreadScheduledExecutor());
    this.executorService.ifPresent(service -> {
      service.schedule(() -> callback.onTimeout(), timeout.getMilliseconds(),
          TimeUnit.MILLISECONDS);
    });

  }

  /**
   * Stop the timeout supervision.
   */
  public void stopSupervision() {
    this.executorService.ifPresent(service -> service.shutdownNow());
    this.executorService = Optional.empty();
  }

  /**
   * This interface describes the contract for timeout reaction mechanism. When timeout is happened,
   * the {@link TimeoutSupervisor} will call {@link TimeoutCallback#onTimeout()} method, in which
   * you have to put your logic for timeout processing.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  public interface TimeoutCallback {
    /**
     * Handle timeout.
     */
    public void onTimeout();
  }
}
