package com.lunatic.mlx.kddcup99.mllib.streaming

import com.lunatic.mlx.kddcup99.mllib.metadata.KMeansXMD
import org.apache.spark.streaming.scheduler._

/**
  * Maybe we can use this as a entry/exit point for the model loading/saving
  *
  * @param sms
  */
case class StreamingKMeansListener(sms: List[KMeansXMD]) extends StreamingListener {

  /** Called when a batch of jobs has been submitted for processing. */
  override def onBatchSubmitted(batchSubmitted: StreamingListenerBatchSubmitted) = {}

  /** Called when processing of a batch of jobs has started.  */
  override def onBatchStarted(batchStarted: StreamingListenerBatchStarted) = {
    // Maybe check for new KMeans models and refresh out list?
  }

  /** Called when processing of a batch of jobs has completed. */
  override def onBatchCompleted(batchCompleted: StreamingListenerBatchCompleted) = {}

  /** Called when a receiver has been started */
  override def onReceiverStarted(receiverStarted: StreamingListenerReceiverStarted) = {}

  /** Called when a receiver has reported an error */
  override def onReceiverError(receiverError: StreamingListenerReceiverError) = {}

  /** Called when a receiver has been stopped */
  override def onReceiverStopped(receiverStopped: StreamingListenerReceiverStopped) = {}

}
