#pragma version (1)
#pragma rs java_package_name ( com.example.ColorStudio)
#pragma rs_fp_relaxed

#define UCHAR_MIN 0
#define UCHAR_MAX 255




#pragma rs reduce(findMinMax) \
    initializer(fMMInit) accumulator(fMMAccumulator) \
     combiner(fMMCombiner) outconverter(fMMOutConverter)

#define SIZE_COLOR_MIN_MAX 3

typedef uchar2 minMaxArr[SIZE_COLOR_MIN_MAX];

bool GRAYONLY;
bool HSV_MODE;

static void fMMInit(minMaxArr* accum) {


    for(uchar i = 0; i < SIZE_COLOR_MIN_MAX; ++i){
        ((*accum)[i]).x = UCHAR_MAX;
        ((*accum)[i]).y = UCHAR_MIN;
        if(GRAYONLY || HSV_MODE){ //We take only 1 channel;
            break;
        }

    }
}

static void fMMAccumulator(minMaxArr *minMaxRGB, uchar4 in) {

  if (HSV_MODE) { //Convert to value
    uchar value;
    value = in.r > in.g ? in.r : in.g;
    value = value > in.b ? value : in.b;
      if (value <= (*minMaxRGB)[0].x)
        (*minMaxRGB)[0].x = value;
      if (value >= (*minMaxRGB)[0].y)
        (*minMaxRGB)[0].y = value;
    return;
  }



  if (in.r <= (*minMaxRGB)[0].x)
    (*minMaxRGB)[0].x = in.r;
  if (in.r >= (*minMaxRGB)[0].y)
    (*minMaxRGB)[0].y = in.r;
  if (GRAYONLY){
    return;
   }

  if (in.g <= (*minMaxRGB)[1].x)
    (*minMaxRGB)[1].x = in.g;
  if (in.g >= (*minMaxRGB)[1].y)
    (*minMaxRGB)[1].y = in.g;

  if (in.b <= (*minMaxRGB)[2].x)
    (*minMaxRGB)[2].x = in.b;
  if (in.b >= (*minMaxRGB)[2].y)
    (*minMaxRGB)[2].y = in.b;


}

static void fMMCombiner(minMaxArr *accum,
                        const minMaxArr *addend) {

  for(uchar i = 0; i < SIZE_COLOR_MIN_MAX; ++i){

      if ((((*accum)[i]).x < 0) || (((*addend)[i]).x < ((*accum)[i]).x))
        (*accum)[i].x = (*addend)[i].x;

      if ((((*accum)[i]).y < 0) || (((*addend)[i]).y > ((*accum)[i]).y))
        (*accum)[i].y = (*addend)[i].y;

      if (GRAYONLY || HSV_MODE){
        break;
      }

   }


}

static void fMMOutConverter(minMaxArr * result, const minMaxArr * accum){
      for(uchar i = 0; i < SIZE_COLOR_MIN_MAX; ++i){
        (*result)[i].x = (*accum)[i].x;
        (*result)[i].y = (*accum)[i].y;
        if (GRAYONLY || HSV_MODE){
            break;
        }

      }
}





