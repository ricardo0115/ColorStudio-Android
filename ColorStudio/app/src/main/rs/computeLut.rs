#pragma version (1)
#pragma rs java_package_name ( com.example.ColorStudio)
#pragma rs_fp_relaxed

#define MIN_MAX_SIZE_RGB 3
#define LUT_SIZE 256

#include "keep_one_color_and_colorize.rs"

//MinMax array
uchar2 minMaxGray;

//MinMax RGBArray
uchar2 minMaxRGB[MIN_MAX_SIZE_RGB];

//LUT RGB with 3 channels
static uchar3 lutRGB [LUT_SIZE];

//LUT single for gray or HSV (Value)
uchar lutSingle[LUT_SIZE];



//For decreasing contrast
bool DECREASE;


void RS_KERNEL computeLutSingle(uchar in, uint32_t x){
    if(!DECREASE)
        //Inverse of augmentation formula
        lutSingle[x] = ((255 * (x - minMaxGray.x)) / (minMaxGray.y - minMaxGray.x));
    else
        //Dynamic Linear Extension formula
        lutSingle[x] = ((x * (minMaxGray.y - minMaxGray.x)) / 255) + minMaxGray.x;

}


//Compute Lut
uchar4 RS_KERNEL assignLutSingle(uchar4 in){

    uchar4 out;

    out.rgb = lutSingle[in.r];

    out.a = in.a;



    return out;

}


//Compute LUT fot value HSV
uchar4 RS_KERNEL assignLutHSV(uchar4 in){
    float4 out = rsUnpackColor8888(in);
    out = RGBtoHSV(out); //Change to HSV
    out.s2 = lutSingle[(uint32_t) (out.s2 * 255)]; //Search new HSV value
    out.s2 /= 255.0; //back to 0..1 range
    out = HSVtoRGB(out);

    return rsPackColorTo8888(out.r , out.g , out.b , out.a);


}

//Compute LUT for RGB
void RS_KERNEL computeLutRGB(uchar in, uint32_t x){
    if(!DECREASE){
        lutRGB[x].r = ((255 * (x - minMaxRGB[0].x)) / (minMaxRGB[0].y - minMaxRGB[0].x));
        lutRGB[x].g = ((255 * (x - minMaxRGB[1].x)) / (minMaxRGB[1].y - minMaxRGB[1].x));
        lutRGB[x].b = ((255 * (x - minMaxRGB[2].x)) / (minMaxRGB[2].y - minMaxRGB[2].x));
    }else{
        lutRGB[x].r =  ((x * (minMaxRGB[0].y - minMaxRGB[0].x)) / 255) + minMaxRGB[0].x;
        lutRGB[x].g =  ((x * (minMaxRGB[1].y - minMaxRGB[1].x)) / 255) + minMaxRGB[1].x;
        lutRGB[x].b =  ((x * (minMaxRGB[2].y - minMaxRGB[2].x)) / 255) + minMaxRGB[2].x;
    }

}

//Assign LUT into image
uchar4 RS_KERNEL assignLutRGB(uchar4 in) {
    uchar4 out;
    out.r =  lutRGB[in.r].r;
    out.g =  lutRGB[in.g].g;
    out.b =  lutRGB[in.b].b;

    out.a = in.a;

    return out;

}

//LUT for RGB luminance computation
uchar4 RS_KERNEL assignLutRGBAverage(uchar4 in) {
    uchar4 out;
    out.r =  lutSingle[in.r];
    out.g =  lutSingle[in.g];
    out.b =  lutSingle[in.b];

    out.a = in.a;

    return out;

}

void processRGB(rs_allocation inputImage, rs_allocation outputImage){

    rs_allocation lutRGBin = rsCreateAllocation_uchar(256);
    rsForEach(computeLutRGB, lutRGBin);
    rsForEach(assignLutRGB, inputImage, outputImage);

}

void processGray(rs_allocation inputImage, rs_allocation outputImage){
    rs_allocation lutSingleIn = rsCreateAllocation_uchar(256);
    rsForEach(computeLutSingle, lutSingleIn);

    rsForEach(assignLutSingle,inputImage,outputImage);
}

void processHSV(rs_allocation inputImage, rs_allocation outputImage){
    rs_allocation lutSingleIn = rsCreateAllocation_uchar(256);
    rsForEach(computeLutSingle,lutSingleIn); //We can use the same function to gray, because we use the same minMaxValue

    rsForEach(assignLutHSV,inputImage,outputImage);
}













