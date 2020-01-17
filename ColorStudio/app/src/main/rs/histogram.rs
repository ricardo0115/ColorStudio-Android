#pragma version (1)
#pragma rs java_package_name ( com.example.ColorStudio)
#pragma rs_fp_relaxed

//Accumulator declaration
#pragma rs reduce(histogram) \
    accumulator(histAccum) combiner(histCombine)

//Histogram size
#define H_SIZE 256

//Global variables to choose mode
bool GRAY = false;
bool HSV = false;
bool RGB = false;

//Histogram type declaration and size
typedef uint64_t Histogram[H_SIZE];
uint32_t size;

//LUT table
typedef uchar LUTret[H_SIZE];

//Cumulate bins with mode
static void histAccum(Histogram *h, uchar4 in) {
    if (GRAY){
        ++(*h)[in.r];
    }else if (HSV){
        uchar value;
        value = in.r > in.g ? in.r : in.g;
        value = value > in.b ? value : in.b;
        ++(*h)[value];
    }else if (RGB){
        ++(*h)[in.r];
        ++(*h)[in.g];
        ++(*h)[in.b];
    }

}

//Combine fonction
static void histCombine(Histogram *accum,
                       const Histogram *addend) {
  for (int i = 0; i < H_SIZE; ++i)
    (*accum)[i] += (*addend)[i];
}

//Reduction kernel to compute LUTCumulated
#pragma rs reduce(LUTCumulatedHistogram) \
    accumulator(histAccum) combiner(histCombine) \
    outconverter(modeOutConvert)

//Compute cumulated histogram
static void modeOutConvert(LUTret *result, const Histogram *h) {
    uint64_t acc = 0;
    uint64_t hValue;
    for (int ng = 0; ng < H_SIZE; ng++) {
        hValue = (*h)[ng];
        if (RGB)
            hValue /= 3; //(*h)[ng] /= 3 do not work cause it is a read-only variable
        acc += hValue; //Cumulative histogram
        (*result)[ng] = (int) ((acc * H_SIZE) / (size)); //Histogram Equalization formula
    }
}





