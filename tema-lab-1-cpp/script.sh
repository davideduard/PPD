#!/bin/bash

echo -e "1000 lines & cols 5 convolution dimension and 16 threads\n"
./matrix-convolution-conversion --lines 1000 --columns 1000 --threads 16 --convolution 5 --regenerate true
echo -e "\n"
for i in {1..9}; do
    ./matrix-convolution-conversion --lines 1000 --columns 1000 --threads 16 --convolution 5
    echo -e "\n"
done

echo -e "10 lines & 10000 cols 5 convolution dimension and 2 threads\n"
./matrix-convolution-conversion --lines 10 --columns 10000 --threads 2 --convolution 5 --regenerate true
echo -e "\n"
for i in {1..9}; do
    ./matrix-convolution-conversion --lines 10 --columns 10000 --threads 2 --convolution 5
    echo -e "\n"
done

echo -e "10 lines & 10000 cols 5 convolution dimension and 4 threads\n"
./matrix-convolution-conversion --lines 10 --columns 10000 --threads 4 --convolution 5 --regenerate true
echo -e "\n"
for i in {1..9}; do
    ./matrix-convolution-conversion --lines 10 --columns 10000 --threads 4 --convolution 5
    echo -e "\n"
done

echo -e "10 lines & 10000 cols 5 convolution dimension and 8 threads\n"
./matrix-convolution-conversion --lines 10 --columns 10000 --threads 8 --convolution 5 --regenerate true
echo -e "\n"
for i in {1..9}; do
    ./matrix-convolution-conversion --lines 10 --columns 10000 --threads 8 --convolution 5
    echo -e "\n"
done

echo -e "10 lines & 10000 cols 5 convolution dimension and 16 threads\n"
./matrix-convolution-conversion --lines 10 --columns 10000 --threads 16 --convolution 5 --regenerate true
echo -e "\n"
for i in {1..9}; do
    ./matrix-convolution-conversion --lines 10 --columns 10000 --threads 16 --convolution 5
    echo -e "\n"
done

echo -e "10000 lines & 10 cols 5 convolution dimension and 2 threads\n"
./matrix-convolution-conversion --lines 10000 --columns 10 --threads 2 --convolution 5 --regenerate true
echo -e "\n"
for i in {1..9}; do
    ./matrix-convolution-conversion --lines 10000 --columns 10 --threads 2 --convolution 5
    echo -e "\n"
done

echo -e "10000 lines & 10 cols 5 convolution dimension and 4 threads\n"
./matrix-convolution-conversion --lines 10000 --columns 10 --threads 4 --convolution 5 --regenerate true
echo -e "\n"
for i in {1..9}; do
    ./matrix-convolution-conversion --lines 10000 --columns 10 --threads 4 --convolution 5
    echo -e "\n"
done

echo -e "10000 lines & 10 cols 5 convolution dimension and 8 threads\n"
./matrix-convolution-conversion --lines 10000 --columns 10 --threads 16 --convolution 5 --regenerate true
echo -e "\n"
for i in {1..9}; do
    ./matrix-convolution-conversion --lines 10000 --columns 10 --threads 2 --convolution 5
    echo -e "\n"
done

echo -e "10000 lines & 10 cols 5 convolution dimension and 16 threads\n"
./matrix-convolution-conversion --lines 10000 --columns 10 --threads 16 --convolution 5 --regenerate true
echo -e "\n"
for i in {1..9}; do
    ./matrix-convolution-conversion --lines 10000 --columns 10 --threads 2 --convolution 5
    echo -e "\n"
done
