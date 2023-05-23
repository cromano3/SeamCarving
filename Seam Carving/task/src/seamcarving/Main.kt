package seamcarving
import java.awt.Color
import java.awt.Color.blue
import java.awt.Color.green
import java.awt.Color.red
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.util.Scanner
import javax.imageio.ImageIO
import kotlin.math.sqrt

fun main(args: Array<String>) {
    val scanner = Scanner(System.`in`)

//    println("Enter rectangle width:")
//    val width = scanner.nextLine().toInt()
//    println("Enter rectangle height:")
//    val height = scanner.nextLine().toInt()
//    println("Enter output image name:")
//    val fileName = scanner.nextLine()

    var inputFileName = ""
    var outputFileName = ""
    var numVert = 0
    var numHor = 0

    for (x in args.indices) {
        when (args[x]) {
            "-in" -> { inputFileName = args[x + 1] }
            "-out" -> { outputFileName = args[x + 1] }
            "-width" -> { numVert = args[x + 1].toInt() }
            "-height" -> { numHor = args[x + 1].toInt() }
        }
    }

    try{

        val inputFile = File(inputFileName)
        var myImage: BufferedImage = ImageIO.read(inputFile)


        for(x in 0 until numVert){
            myImage = removeSeem(myImage)
        }

        myImage = transpose(myImage)

        for(x in 0 until numHor){
            myImage = removeSeem(myImage)
        }

        myImage = transpose(myImage)

        val outputFile = File(outputFileName)
        ImageIO.write(myImage, "png", outputFile)

    }
    catch(e: IOException){
        println("Can't read input file!")
    }


}

fun removeSeem(myImage: BufferedImage): BufferedImage{

    val energies = Array(myImage.height) { Array(myImage.width) { 0.0 } }

    for(y in 0 until myImage.height){
        for(x in 0 until myImage.width){

            val colorx1 = Color(myImage.getRGB(if(x == 0) x else if(x == myImage.width - 1) x - 2 else x - 1, y))
            val colorx2 = Color(myImage.getRGB(if(x == 0) x + 2 else if(x == myImage.width - 1) x else x + 1, y))
            val colory1 = Color(myImage.getRGB(x, if(y == 0) y else if(y == myImage.height - 1) y - 2 else y - 1))
            val colory2 = Color(myImage.getRGB(x, if(y == 0) y + 2 else if(y == myImage.height - 1) y else y + 1))

            val xRed = colorx1.red - colorx2.red
            val xGreen = colorx1.green - colorx2.green
            val xBlue = colorx1.blue - colorx2.blue

            val xGrad = (xRed*xRed) + (xGreen*xGreen) + (xBlue*xBlue)

            val yRed = colory1.red - colory2.red
            val yGreen = colory1.green - colory2.green
            val yBlue = colory1.blue - colory2.blue

            val yGrad = (yRed*yRed) + (yGreen*yGreen) + (yBlue*yBlue)

            val energy = sqrt(xGrad.toDouble() + yGrad.toDouble())

            energies[y][x] = energy

        }
    }

    val distances = Array(myImage.height) { i ->
        Array(myImage.width) {if(i == 0) energies[i][it] else Double.POSITIVE_INFINITY}
    }


    val predecessors = Array(myImage.height) { Array(myImage.width) { Pair(-1, -1) } }


    for(y in 1 until distances.size){
        for(x in 0 until distances[0].size){

            val above = distances[y - 1][x]
            val aboveLeft = if(x != 0) distances[y - 1][x - 1] else Double.POSITIVE_INFINITY
            val aboveRight = if(x != distances[0].size - 1) distances[y - 1][x + 1] else Double.POSITIVE_INFINITY


            val minDistance = minOf(above, aboveLeft, aboveRight)
            distances[y][x] = energies[y][x] + minDistance

            when(minDistance){
                above -> predecessors[y][x] = Pair(y - 1, x)
                aboveLeft -> predecessors[y][x] = Pair(y - 1, x - 1)
                aboveRight -> predecessors[y][x] = Pair(y - 1, x + 1)
            }

        }
    }



    var y = myImage.height - 1
    var x = distances.last().withIndex().minByOrNull { it.value }?.index

    val pixelsToRemove = mutableListOf<Pair<Int,Int>>()

    while (y >= 0 && x != null) {
//        myImage.setRGB(x, y, Color.red.rgb)
        pixelsToRemove.add(Pair(y, x))
        y = predecessors[y][x].first.also{x = predecessors[y][x!!].second}
    }

    val newImage = BufferedImage(myImage.width - 1, myImage.height, BufferedImage.TYPE_INT_RGB)



    for(y in 0 until myImage.height) {
        var factor = 0
        for (x in 0 until myImage.width) {
            if(pixelsToRemove.contains(Pair(y,x))){
                factor = -1
                continue
            }
            else{
                newImage.setRGB(x + factor, y, myImage.getRGB(x, y))
            }
        }
    }

    return newImage

}

fun transpose(image: BufferedImage): BufferedImage {
    val newImage = BufferedImage(image.height, image.width, BufferedImage.TYPE_INT_RGB)
    with (image) {
        for (x in 0 until width)
            for (y in 0 until height) {
                val color = image.getRGB(x, y)
                newImage.setRGB(y, x, color)
            }
    }
    return newImage
}
