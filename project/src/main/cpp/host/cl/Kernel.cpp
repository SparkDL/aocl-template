//
// Created by pcz on 18-5-17.
//
#include "Kernel.h"

namespace saoclib {

    Kernel::Kernel(const CLBinary *binary,
                   const cl_device_id device,
                   const std::string &kernelName,
                   const KernelArgSignature *signatures,
                   unsigned numArgs)
            : binary(binary),
              device(device),
              kernelName(kernelName),
              numArgs(numArgs),
              buffers(numArgs),
              bufferSizes(numArgs) {
        for (int i = 0; i < numArgs; i++) {
            buffers[i] = NULL;
            bufferSizes[i] = 0;
        }
        cl_int status;

        // Create kernel
        kernel = clCreateKernel(binary->getProgram(), kernelName.c_str(), &status);
        checkError(status, "Failed to create kernel");

        // Create buffer for arguments
        this->signatures.reset(numArgs);
        for (unsigned i = 0; i < numArgs; i++) {
            auto sig = signatures[i];
            this->signatures[i] = sig;
        }
    }

    Kernel::~Kernel() {
        cleanup();
    }

    void Kernel::cleanup() {
        if (kernel) {
            clReleaseKernel(kernel);
            kernel = NULL;
        }
        for (auto &mem:buffers) {
            if (mem) {
                clReleaseMemObject(mem);
                mem = NULL;
            }
        }
    }

}