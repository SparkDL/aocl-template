package com.pzque.sparkdl.models

import com.intel.analytics.bigdl._
import com.intel.analytics.bigdl.nn.abstractnn.AbstractModule
import com.intel.analytics.bigdl.nn.{Module => _, _}
import com.intel.analytics.bigdl.tensor.TensorNumericMath.TensorNumeric
import com.intel.analytics.bigdl.tensor.{Storage, Tensor}
import com.intel.analytics.bigdl.utils.Table

import scala.language.existentials
import scala.reflect.ClassTag

class deepspeech2[T: ClassTag](depth: Int = 1, isPaperVersion: Boolean = false)
                              (implicit ev: TensorNumeric[T]) {

  /**
    * The configuration of convolution for dp2.
    */
  val nInputPlane = 1
  val nOutputPlane = 1152
  val kW = 11
  val kH = 13
  val dW = 3
  val dH = 1
  val padW = 5
  val padH = 0
  val conv = SpatialConvolution(nInputPlane, nOutputPlane,
    kW, kH, dW, dH, padW, padH)

  val nOutputDim = 2
  val outputHDim = 3
  val outputWDim = 4
  val inputSize = nOutputPlane
  val hiddenSize = nOutputPlane
  val nChar = 29

  /**
    * append BiRNN layers to the deepspeech model.
    * When isPaperVersion is set to be true, the sequential will construct a DS2 w.r.t implementation from Paper.
    * Otherwise, it will construct a Nervana's DS2 version.
    *
    * @param inputSize
    * @param hiddenSize
    * @param curDepth
    * @return
    */
  def addBRNN(inputSize: Int, hiddenSize: Int, curDepth: Int)
  : Module[T] = {
    val layers = Sequential()
    if (curDepth == 1) {
      layers
        .add(ConcatTable()
          .add(Identity[T]())
          .add(Identity[T]()))
    } else {
      layers
        .add(BifurcateSplitTable[T](3))
    }
    layers
      .add(ParallelTable[T]()
        .add(TimeDistributed[T](Linear[T](inputSize, hiddenSize, withBias = false)))
        .add(TimeDistributed[T](Linear[T](inputSize, hiddenSize, withBias = false))))
      .add(JoinTable[T](2, 2))
      .add(TimeDistributed(BatchNormalization[T](hiddenSize * 2, eps = 0.001, momentum = 0.1, affine = false)))
      .add(BiRecurrent[T](JoinTable[T](2, 2).asInstanceOf[AbstractModule[Table, Tensor[T], T]], isSplitInput = true)
        .add(RnnCellDS[T](hiddenSize, hiddenSize, HardTanh[T](0, 20, true)))
        .setName("birnn" + depth))
    layers
  }

  val brnn = Sequential()
  var i = 1
  while (i <= depth) {
    if (i == 1) {
      brnn.add(addBRNN(inputSize, hiddenSize, i))
    } else {
      brnn.add(addBRNN(hiddenSize, hiddenSize, i))
    }
    i += 1
  }

  val brnnOutputSize = if (isPaperVersion) hiddenSize else hiddenSize * 2
  val linear1 = TimeDistributed[T](Linear[T](brnnOutputSize, hiddenSize, withBias = false))
  val linear2 = TimeDistributed[T](Linear[T](hiddenSize, nChar, withBias = false))

  /**
    * The deep speech2 model.
    * ****************************************************************************************
    *
    * Convolution -> ReLU -> BiRNN (9 layers) -> Linear -> ReLUClip (HardTanh) -> Linear
    *
    * ****************************************************************************************
    */
  val model = Sequential[T]()
    .add(conv)
    .add(ReLU[T]())
    .add(Transpose(Array((nOutputDim, outputWDim), (outputHDim, outputWDim))))
    .add(Squeeze(4))
    .add(brnn)
    .add(linear1)
    .add(HardTanh[T](0, 20, true))
    .add(linear2)

  def reset(): Unit = {
    conv.weight.fill(ev.fromType[Float](0.0F))
    conv.bias.fill(ev.fromType[Float](0.0F))
  }

  def setConvWeight(weights: Array[T]): Unit = {
    val temp = Tensor[T](Storage(weights), 1, Array(1, 1152, 1, 13, 11))
    conv.weight.set(Storage[T](weights), 1, conv.weight.size())
  }

  /**
    * load in the nervana's dp2 BiRNN model parameters
    *
    * @param weights
    */
  def setBiRNNWeight(weights: Array[Array[T]]): Unit = {
    val parameters = brnn.parameters()._1
    // six tensors per brnn layer
    val numOfParams = 6
    for (i <- 0 until depth) {
      var offset = 1
      for (j <- 0 until numOfParams) {
        val length = parameters(i * numOfParams + j).nElement()
        val size = parameters(i * numOfParams + j).size
        parameters(i * numOfParams + j).set(Storage[T](weights(i)), offset, size)
        offset += length
      }
    }
  }

  /**
    * load in the nervana's dp2 Affine model parameters
    *
    * @param weights
    * @param num
    */
  def setLinear0Weight(weights: Array[T], num: Int): Unit = {
    if (num == 0) {
      linear1.parameters()._1(0)
        .set(Storage[T](weights), 1, Array(1152, 2304))
    } else {
      linear2.parameters()._1(0)
        .set(Storage[T](weights), 1, Array(29, 1152))
    }
  }
}