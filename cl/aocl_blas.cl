__kernel void vsAdd(__global const float *restrict x,
                    __global const float *restrict y,
                    __global float *restrict z) {
    // get index of the work item
    int index = get_global_id(0);
    z[index] = x[index] + y[index];
}

__kernel void vsSub(__global const float *restrict x,
                    __global const float *restrict y,
                    __global float *restrict z) {
    // get index of the work item
    int index = get_global_id(0);
    z[index] = x[index] - y[index];
}

__kernel void vsMul(__global const float *restrict x,
                    __global const float *restrict y,
                    __global float *restrict z) {
    // get index of the work item
    int index = get_global_id(0);
    z[index] = x[index] * y[index];
}

__kernel void vsDiv(__global const float *restrict x,
                    __global const float *restrict y,
                    __global float *restrict z) {
    // get index of the work item
    int index = get_global_id(0);
    z[index] = x[index] / y[index];
}

__kernel void vsPowx(__global const float *restrict x,
                     const float y,
                     __global float *restrict z) {
    // get index of the work item
    int index = get_global_id(0);
    z[index] = pow(x[index], y);
}

__kernel void vsLn(__global const float *restrict x,
                   __global float *restrict z) {
    // get index of the work item
    int index = get_global_id(0);
    z[index] = log(x[index]);
}

__kernel void vsExp(__global const float *restrict x,
                    __global float *restrict z) {
    // get index of the work item
    int index = get_global_id(0);
    z[index] = exp(x[index]);
}

__kernel void vsSqrt(__global const float *restrict x,
                     __global float *restrict z) {
    // get index of the work item
    int index = get_global_id(0);
    z[index] = sqrt(x[index]);
}


__kernel void vsTanh(__global const float *restrict x,
                     __global float *restrict z) {
    // get index of the work item
    int index = get_global_id(0);
    z[index] = tanh(x[index]);
}

__kernel void vsLog1p(__global const float *restrict x,
                      __global float *restrict z) {
    // get index of the work item
    int index = get_global_id(0);
    z[index] = log1p(x[index]);
}

__kernel void vsAbs(__global const float *restrict x,
                    __global float *restrict z) {
    // get index of the work item
    int index = get_global_id(0);
    z[index] = fabs(x[index]);
}