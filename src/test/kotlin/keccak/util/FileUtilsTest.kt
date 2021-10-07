package keccak.util

import io.kotest.core.spec.style.FunSpec
import mu.KotlinLogging
import java.io.File

class FileUtilsTest : FunSpec({
    val logger = KotlinLogging.logger {}

    test("toXorEquationSystem") {
        logger.info("loading...")
        File("D:\\test\\leftRight.txt").toXorEquationSystem(76777, 116288, false)
        logger.info("loaded")
    }

    test("toAndEquationSystem") {
        logger.info("loading...")
        val system = File("D:\\test\\andEquations.txt").toAndEquationSystem(37057, 116288, true)
        logger.info("loaded")
        system.toFile(File("D:\\test\\andEquations2.txt"), humanReadable = true)
    }

    test("fromBinaryXorEquationSystemToHumanReadable") {
        File("D:\\test\\leftRight.txt").fromBinaryXorEquationSystemToHumanReadable(File("D:\\test\\leftRightHuman.txt"))
    }
})
