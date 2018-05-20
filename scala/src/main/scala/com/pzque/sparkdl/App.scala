import com.pzque.sparkdl.saoclib._

object App {
  def main(args: Array[String]): Unit = {
    testCL()
  }

  def testCL() = {
    System.loadLibrary("saoclib")

    // init opencl essentials
    val env = new CLEnv()
    env.initOpenCL()

    // construct an opencl image from file path
    val image = new CLImage(env, "/home/pcz/Projects/saoclib/bin/vector_add")

    // get available devices
    val device_id_list = env.getDeviceIDList.get
    print("devices: ")
    println(device_id_list.mkString("[", ",", "]"))
    val device_id = device_id_list(0)

    // deploy the image to specific devices
    image.deployImage(device_id_list)

    // construct a kernel
    val N = 1000000
    val limits: Array[KernelArgLimit] = Array(
      limit(c_array(c_float, N), mode_input),
      limit(c_array(c_float, N), mode_input),
      limit(c_array(c_float, N), mode_output)
    )
    val kernel = new NDRangeKernel(
      work_dim = 1,
      global_work_size_list = Array(N),
      local_work_size_list = null,
      cl_image = image,
      device_id = device_id,
      kernel_name = "vector_add",
      arg_limits = limits
    )

    // prepare kernel arguments
    val a: ArgArray[Float] = ArgArray.fill(N)(1f)(mode_input)
    val b: ArgArray[Float] = ArgArray.fill(N)(2f)(mode_input)
    val c: ArgArray[Float] = ArgArray.fill(N)(0f)(mode_output)

    // call kernel
    var start, end: Long = 0
    start = System.currentTimeMillis()
    kernel.call(a, b, c)
    end = System.currentTimeMillis()
    println(s"Kernel cost: ${end - start}ms")
    println("\n\n")
  }
}