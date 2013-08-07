package kamon.instrumentation

import org.aspectj.lang.annotation._
import kamon.{Kamon, TraceContext}
import org.aspectj.lang.ProceedingJoinPoint
import scala.Some

/**
 *  Marker interface, just to make sure we don't instrument all the Runnables in the classpath.
 */
trait TraceContextAwareRunnable extends Runnable {}


@Aspect("perthis(instrumentedRunnableCreation())")
class RunnableInstrumentation {

  /**
   *  These are the Runnables that need to be instrumented and make the TraceContext available
   *  while their run method is executed.
   */
  @DeclareMixin("scala.concurrent.impl.CallbackRunnable || scala.concurrent.impl.Future.PromiseCompletingRunnable")
  def onCompleteCallbacksRunnable: TraceContextAwareRunnable = null


  /**
   *  Pointcuts
   */

  @Pointcut("execution(kamon.instrumentation.TraceContextAwareRunnable+.new(..))")
  def instrumentedRunnableCreation(): Unit = {}

  @Pointcut("execution(* kamon.instrumentation.TraceContextAwareRunnable.run())")
  def runnableExecution() = {}


  /**
   *  Aspect members
   */

  private val traceContext = Kamon.context


  /**
   *  Advices
   */
  import kamon.TraceContextSwap.withContext

  @Before("instrumentedRunnableCreation()")
  def beforeCreation = {
    //println((new Throwable).getStackTraceString)
  }


  @Around("runnableExecution()")
  def around(pjp: ProceedingJoinPoint) = {
    import pjp._

    withContext(traceContext, proceed())
  }

}
