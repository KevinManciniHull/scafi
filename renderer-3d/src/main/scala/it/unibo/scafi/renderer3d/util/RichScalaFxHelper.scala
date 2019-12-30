/*
 * Copyright (C) 2016-2017, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENCE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package it.unibo.scafi.renderer3d.util

import it.unibo.scafi.renderer3d.node.NetworkNode
import org.scalafx.extras.{onFX, onFXAndWait}
import scalafx.geometry.{Point2D, Point3D}
import scalafx.scene.transform.Rotate

/** Object that contains some of the required implicit classes to enrich the language (using "Pimp my Library") when
 * working with ScalaFx and JavaFx. */
private[util] trait RichScalaFxHelper {
  private val X_AXIS_2D = new Point2D(1, 0)

  implicit class RichJavaPoint3D(point: javafx.geometry.Point3D) {
    /** Converts the javafx.geometry.Point3D to scalafx.geometry.Point3D
     * @return the point as a scalafx.geometry.Point3D */
    final def toScalaPoint: Point3D = new Point3D(point)
  }

  implicit class RichPoint2D(point: Point2D) {
    /** Calculates the angle between the point(seen as a 2D direction vector) and the other specified 2D direction.
     * @param otherPoint the other point, seen as a 2D direction vector
     * @return the angle between the two 2D directions */
    final def eulerAngleTo(otherPoint: Point2D): Double = {
      val determinant = point.x * otherPoint.y - point.y * otherPoint.x
      FastMath.atan2(determinant.toFloat, point.dotProduct(otherPoint).toFloat).toDegrees
    }
  }

  implicit class RichPoint3D(point: Point3D) {
    /** @return the negated 3D direction */
    final def negate: Point3D = point * -1

    /** @return the point converted to a Product3 of Double */
    final def toProduct: Product3[Double, Double, Double] = (point.x, point.y, point.z)

    /** @param value the scalar value to multiply
     * @return the 3d direction vector multiplied by the provided scalar value */
    final def *(value: Double): Point3D = point.multiply(value).toScalaPoint

    /** @param value the scalar value to calculate the division
     * @return the 3d direction vector divided by the provided scalar value */
    final def /(value: Double): Point3D = new Point3D(point.x / value, point.y / value, point.z / value)

    /** @param otherPoint the 3d direction to be added
     * @return the 3d direction vector that is the sum of the two 3d direction vectors */
    final def +(otherPoint: Point3D): Point3D = point.add(otherPoint).toScalaPoint

    /** @param otherPoint the 3d direction to be subtracted
     * @return the 3d direction vector that is the result of subtraction between the two 3d direction vectors */
    final def -(otherPoint: Point3D): Point3D = point.subtract(otherPoint).toScalaPoint
  }

  implicit class RichJavaNode(node: javafx.scene.Node) {
    /** See [[RichScalaFx.RichNode.lookAtOnXZPlane]] */
    final def lookAtOnXZPlane(point: Point3D): Unit = {
      val yAngle = getLookAtAngleOnXZPlane(point)
      onFX {node.setRotationAxis(Rotate.YAxis); node.setRotate(yAngle)}
    }

    /** See [[RichScalaFx.RichNode.getLookAtAngleOnXZPlane]] */
    final def getLookAtAngleOnXZPlane(point: Point3D): Double = {
      val nodePosition = node.getPosition
      val directionOnXZPlane = new Point2D(point.x - nodePosition.x, point.z - nodePosition.z)
      directionOnXZPlane.eulerAngleTo(X_AXIS_2D) - 90
    }

    /** See [[RichScalaFx.RichNode.getPosition]] */
    final def getPosition: Point3D = {
      val transform = onFXAndWait(node.getLocalToSceneTransform)
      new Point3D(transform.getTx, transform.getTy, transform.getTz)
    }

    /** See [[RichScalaFx.RichNode.getScreenPosition]] */
    final def getScreenPosition: Point2D = {
      val screenBounds = onFXAndWait(node.localToScreen(node.getBoundsInLocal))
      new Point2D(screenBounds.getMinX, screenBounds.getMinY)
    }

    /** See [[RichScalaFx.RichNode.moveTo]] */
    final def moveTo(position: Point3D): Unit = onFX {
      node.setTranslateX(position.x)
      node.setTranslateY(position.y)
      node.setTranslateZ(position.z)
    }

    /** See [[RichScalaFx.RichNode.rotateOnSelf]] */
    final def rotateOnSelf(angle: Double, axis: Point3D): Unit =
      onFX {node.getTransforms.add(new Rotate(angle, 0, 0, 0, axis))}

    /** See [[RichScalaFx.RichNode.toNetworkNode]] */
    final def toNetworkNode: NetworkNode = node match {case networkNode: NetworkNode => networkNode}

    /** See [[RichScalaFx.RichNode.setScale]] */
    final def setScale(scale: Double): Unit = onFX {
      node.setScaleX(scale)
      node.setScaleY(scale)
      node.setScaleZ(scale)
    }

    /** See [[RichScalaFx.RichNode.getYRotationAngle]] */
    final def getYRotationAngle: Double = {
      val zx = node.getLocalToSceneTransform.getMzx
      val zz = node.getLocalToSceneTransform.getMzz
      FastMath.atan2(-zz.toFloat, zx.toFloat).toDegrees
    }
  }

}
