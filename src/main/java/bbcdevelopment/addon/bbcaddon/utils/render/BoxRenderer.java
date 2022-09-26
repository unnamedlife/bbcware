package bbcdevelopment.addon.bbcaddon.utils.render;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.Mesh;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.util.math.Box;

public class BoxRenderer {
    public static void box(Render3DEvent event, Box box, Color side, Color line, ShapeMode shapeMode){
        event.renderer.box(box, side, line, shapeMode, 0);
    }

    public static void box(Render3DEvent event, Box box, Color side, Color line, ShapeMode shapeMode, BoxRendererMethod boxRendererMethod){
        switch (boxRendererMethod){
            case Box -> box(event, box, side, line, shapeMode);
            case Patter1 -> box1(event, box, side, line, shapeMode);
            case Patter2 -> box2(event, box, side, line, shapeMode);
            case Patter3 -> box3(event, box, side, line, shapeMode);
            case Pattern4 -> box4(event, box, side, line, shapeMode);
            case Pattern5 -> box5(event, box, side, line, shapeMode);
        }
    }

    private static void box1(Render3DEvent event, Box box, Color side, Color line, ShapeMode shapeMode){
        Mesh lines = event.renderer.lines;
        Mesh triangles = event.renderer.triangles;

        if (shapeMode == ShapeMode.Lines || shapeMode == ShapeMode.Both){
            int center = lines.vec3(getCenter(box.minX, box.maxX), getCenter(box.minY, box.maxY), getCenter(box.minZ, box.maxZ)).color(line).next();

            int p1 = lines.vec3(box.minX, box.minY, getCenter(box.minZ, box.maxZ)).color(line).next();
            int p2 = lines.vec3(box.minX, box.maxY, getCenter(box.minZ, box.maxZ)).color(line).next();
            int p3 = lines.vec3(box.minX, getCenter(box.minY, box.maxY), box.minZ).color(line).next();
            int p4 = lines.vec3(box.minX, getCenter(box.minY, box.maxY), box.maxZ).color(line).next();

            int p5 = lines.vec3(getCenter(box.minX, box.maxX), box.minY, box.maxZ).color(line).next();
            int p6 = lines.vec3(getCenter(box.minX, box.maxX), box.maxY, box.maxZ).color(line).next();
            int p7 = lines.vec3(box.maxX, getCenter(box.minY, box.maxY), box.maxZ).color(line).next();

            int p8 = lines.vec3(box.maxX, box.minY, getCenter(box.minZ, box.maxZ)).color(line).next();
            int p9 = lines.vec3(box.maxX, box.maxY, getCenter(box.minZ, box.maxZ)).color(line).next();
            int p10 = lines.vec3(box.maxX, getCenter(box.minY, box.maxY), box.minZ).color(line).next();

            int p11 = lines.vec3(getCenter(box.minX, box.maxX), box.minY, box.minZ).color(line).next();
            int p12 = lines.vec3(getCenter(box.minX, box.maxX), box.maxY, box.minZ).color(line).next();

            lines.line(center, p1);
            lines.line(center, p2);
            lines.line(center, p5);
            lines.line(center, p6);

            lines.line(center, p8);
            lines.line(center, p9);
            lines.line(center, p11);
            lines.line(center, p12);

            lines.line(p1, p3);
            lines.line(p1, p4);
            lines.line(p2, p3);
            lines.line(p2, p4);

            lines.line(p5, p4);
            lines.line(p5, p7);
            lines.line(p6, p4);
            lines.line(p6, p7);

            lines.line(p8, p7);
            lines.line(p8, p10);
            lines.line(p9, p7);
            lines.line(p9, p10);

            lines.line(p11, p3);
            lines.line(p11, p10);
            lines.line(p12, p3);
            lines.line(p12, p10);
        }
        if (shapeMode == ShapeMode.Sides || shapeMode == ShapeMode.Both){
            int p1 = triangles.vec3(box.minX, box.minY, getCenter(box.minZ, box.maxZ)).color(side).next();
            int p2 = triangles.vec3(box.minX, box.maxY, getCenter(box.minZ, box.maxZ)).color(side).next();
            int p3 = triangles.vec3(box.minX, getCenter(box.minY, box.maxY), box.minZ).color(side).next();
            int p4 = triangles.vec3(box.minX, getCenter(box.minY, box.maxY), box.maxZ).color(side).next();

            int p5 = triangles.vec3(getCenter(box.minX, box.maxX), box.minY, box.maxZ).color(side).next();
            int p6 = triangles.vec3(getCenter(box.minX, box.maxX), box.maxY, box.maxZ).color(side).next();
            int p7 = triangles.vec3(box.maxX, getCenter(box.minY, box.maxY), box.maxZ).color(side).next();

            int p8 = triangles.vec3(box.maxX, box.minY, getCenter(box.minZ, box.maxZ)).color(side).next();
            int p9 = triangles.vec3(box.maxX, box.maxY, getCenter(box.minZ, box.maxZ)).color(side).next();
            int p10 = triangles.vec3(box.maxX, getCenter(box.minY, box.maxY), box.minZ).color(side).next();

            int p11 = triangles.vec3(getCenter(box.minX, box.maxX), box.minY, box.minZ).color(side).next();
            int p12 = triangles.vec3(getCenter(box.minX, box.maxX), box.maxY, box.minZ).color(side).next();

            triangles.quad(p2, p3, p12, p2);
            triangles.quad(p1, p3, p11, p2);

            triangles.quad(p9, p10, p12, p9);
            triangles.quad(p8, p10, p11, p8);

            triangles.quad(p9, p7, p6, p9);
            triangles.quad(p5, p7, p8, p5);

            triangles.quad(p2, p4, p6, p2);
            triangles.quad(p1, p4, p5, p1);
        }
    }

    private static void box2(Render3DEvent event, Box box, Color side, Color line, ShapeMode shapeMode){
        Mesh lines = event.renderer.lines;
        Mesh triangles = event.renderer.triangles;
        if (shapeMode == ShapeMode.Lines || shapeMode == ShapeMode.Both){
            int a1 = lines.vec3(box.minX, box.maxY, box.minZ).color(line).next();
            int a2 = lines.vec3(box.minX, box.maxY, box.maxZ).color(line).next();
            int a3 = lines.vec3(box.maxX, box.maxY, box.maxZ).color(line).next();
            int a4 = lines.vec3(box.maxX, box.maxY, box.minZ).color(line).next();

            int a5 = lines.vec3(getPartPlus(box.minX, box.maxX, 5), box.minY, getPartPlus(box.minZ, box.maxZ, 5)).color(line).next();
            int a6 = lines.vec3(getPartPlus(box.minX, box.maxX, 5), box.minY, getPartMinus(box.minZ, box.maxZ, 5)).color(line).next();
            int a7 = lines.vec3(getPartMinus(box.minX, box.maxX, 5), box.minY, getPartMinus(box.minZ, box.maxZ, 5)).color(line).next();
            int a8 = lines.vec3(getPartMinus(box.minX, box.maxX, 5), box.minY, getPartPlus(box.minZ, box.maxZ, 5)).color(line).next();

            lines.line(a1, a2);
            lines.line(a2, a3);
            lines.line(a3, a4);
            lines.line(a4, a1);

            lines.line(a5, a6);
            lines.line(a6, a7);
            lines.line(a7, a8);
            lines.line(a8, a5);

            lines.line(a1, a5);
            lines.line(a2, a6);
            lines.line(a3, a7);
            lines.line(a4, a8);
        }
        if (shapeMode == ShapeMode.Sides || shapeMode == ShapeMode.Both){
            int a1 = triangles.vec3(box.minX, box.maxY, box.minZ).color(side).next();
            int a2 = triangles.vec3(box.minX, box.maxY, box.maxZ).color(side).next();
            int a3 = triangles.vec3(box.maxX, box.maxY, box.maxZ).color(side).next();
            int a4 = triangles.vec3(box.maxX, box.maxY, box.minZ).color(side).next();

            int a5 = triangles.vec3(getPartPlus(box.minX, box.maxX, 5), box.minY, getPartPlus(box.minZ, box.maxZ, 5)).color(side).next();
            int a6 = triangles.vec3(getPartPlus(box.minX, box.maxX, 5), box.minY, getPartMinus(box.minZ, box.maxZ, 5)).color(side).next();
            int a7 = triangles.vec3(getPartMinus(box.minX, box.maxX, 5), box.minY, getPartMinus(box.minZ, box.maxZ, 5)).color(side).next();
            int a8 = triangles.vec3(getPartMinus(box.minX, box.maxX, 5), box.minY, getPartPlus(box.minZ, box.maxZ, 5)).color(side).next();

            triangles.quad(a1, a5, a6, a2);
            triangles.quad(a2, a6, a7, a3);
            triangles.quad(a3, a7, a8, a4);
            triangles.quad(a4, a8, a5, a1);
        }
    }

    private static void box3(Render3DEvent event, Box box, Color side, Color line, ShapeMode shapeMode){
        Mesh lines = event.renderer.lines;
        Mesh triangles = event.renderer.triangles;
        if (shapeMode == ShapeMode.Lines || shapeMode == ShapeMode.Both){
            //1
            int c1 = lines.vec3(getPartPlus(box.minX, box.maxX, 6), getPartPlus(box.minY, box.maxY, 6), box.minZ).color(line).next();
            int c2 = lines.vec3(box.minX, getPartPlus(box.minY, box.maxY, 4), box.minZ).color(line).next();
            int c3 = lines.vec3(getPartPlus(box.minX, box.maxX, 4), box.minY, box.minZ).color(line).next();

            int c4 = lines.vec3(getPartPlus(box.minX, box.maxX, 6), getPartMinus(box.minY, box.maxY, 6), box.minZ).color(line).next();
            int c5 = lines.vec3(box.minX, getPartMinus(box.minY, box.maxY, 4), box.minZ).color(line).next();
            int c6 = lines.vec3(getPartPlus(box.minX, box.maxX, 4), box.maxY, box.minZ).color(line).next();

            int c7 = lines.vec3(getPartMinus(box.minX, box.maxX, 6), getPartMinus(box.minY, box.maxY, 6), box.minZ).color(line).next();
            int c8 = lines.vec3(getPartMinus(box.minX, box.maxX, 4), box.maxY, box.minZ).color(line).next();
            int c9 = lines.vec3(box.maxX, getPartMinus(box.minY, box.maxY, 4), box.minZ).color(line).next();

            int c10 = lines.vec3(getPartMinus(box.minX, box.maxX, 6), getPartPlus(box.minY, box.maxY, 6), box.minZ).color(line).next();
            int c11 = lines.vec3(box.maxX, getPartPlus(box.minY, box.maxY, 4), box.minZ).color(line).next();
            int c12 = lines.vec3(getPartMinus(box.minX, box.maxX, 4), box.minY, box.minZ).color(line).next();

            //2
            int c13 = lines.vec3(getPartPlus(box.minX, box.maxX, 6), getPartPlus(box.minY, box.maxY, 6), box.maxZ).color(line).next();
            int c14 = lines.vec3(box.minX, getPartPlus(box.minY, box.maxY, 4), box.maxZ).color(line).next();
            int c15 = lines.vec3(getPartPlus(box.minX, box.maxX, 4), box.minY, box.maxZ).color(line).next();

            int c16 = lines.vec3(getPartPlus(box.minX, box.maxX, 6), getPartMinus(box.minY, box.maxY, 6), box.maxZ).color(line).next();
            int c17 = lines.vec3(box.minX, getPartMinus(box.minY, box.maxY, 4), box.maxZ).color(line).next();
            int c18 = lines.vec3(getPartPlus(box.minX, box.maxX, 4), box.maxY, box.maxZ).color(line).next();

            int c19 = lines.vec3(getPartMinus(box.minX, box.maxX, 6), getPartMinus(box.minY, box.maxY, 6), box.maxZ).color(line).next();
            int c20 = lines.vec3(getPartMinus(box.minX, box.maxX, 4), box.maxY, box.maxZ).color(line).next();
            int c21 = lines.vec3(box.maxX, getPartMinus(box.minY, box.maxY, 4), box.maxZ).color(line).next();

            int c22 = lines.vec3(getPartMinus(box.minX, box.maxX, 6), getPartPlus(box.minY, box.maxY, 6), box.maxZ).color(line).next();
            int c23 = lines.vec3(box.maxX, getPartPlus(box.minY, box.maxY, 4), box.maxZ).color(line).next();
            int c24 = lines.vec3(getPartMinus(box.minX, box.maxX, 4), box.minY, box.maxZ).color(line).next();

            //3
            int c25 = lines.vec3(box.minX, getPartPlus(box.minY, box.maxY, 6), getPartPlus(box.minZ, box.maxZ, 6)).color(line).next();
            int c26 = lines.vec3(box.minX, getPartPlus(box.minY, box.maxY, 4), box.minZ).color(line).next();
            int c27 = lines.vec3(box.minX, box.minY, getPartPlus(box.minZ, box.maxZ, 4)).color(line).next();

            int c28 = lines.vec3(box.minX, getPartMinus(box.minY, box.maxY, 6), getPartPlus(box.minZ, box.maxZ, 6)).color(line).next();
            int c29 = lines.vec3(box.minX, getPartMinus(box.minY, box.maxY, 4), box.minZ).color(line).next();
            int c30 = lines.vec3(box.minX, box.maxY, getPartPlus(box.minZ, box.maxZ, 4)).color(line).next();

            int c31 = lines.vec3(box.minX, getPartMinus(box.minY, box.maxY, 6), getPartMinus(box.minZ, box.maxZ, 6)).color(line).next();
            int c32 = lines.vec3(box.minX, box.maxY, getPartMinus(box.minZ, box.maxZ, 4)).color(line).next();
            int c33 = lines.vec3(box.minX, getPartMinus(box.minY, box.maxY, 4), box.maxZ).color(line).next();

            int c34 = lines.vec3(box.minX, getPartPlus(box.minY, box.maxY, 6), getPartMinus(box.minZ, box.maxZ, 6)).color(line).next();
            int c35 = lines.vec3(box.minX, getPartPlus(box.minY, box.maxY, 4), box.maxZ).color(line).next();
            int c36 = lines.vec3(box.minX, box.minY, getPartMinus(box.minZ, box.maxZ, 4)).color(line).next();

            //4
            int c37 = lines.vec3(box.maxX, getPartPlus(box.minY, box.maxY, 6), getPartPlus(box.minZ, box.maxZ, 6)).color(line).next();
            int c38 = lines.vec3(box.maxX, getPartPlus(box.minY, box.maxY, 4), box.minZ).color(line).next();
            int c39 = lines.vec3(box.maxX, box.minY, getPartPlus(box.minZ, box.maxZ, 4)).color(line).next();

            int c40 = lines.vec3(box.maxX, getPartMinus(box.minY, box.maxY, 6), getPartPlus(box.minZ, box.maxZ, 6)).color(line).next();
            int c41 = lines.vec3(box.maxX, getPartMinus(box.minY, box.maxY, 4), box.minZ).color(line).next();
            int c42 = lines.vec3(box.maxX, box.maxY, getPartPlus(box.minZ, box.maxZ, 4)).color(line).next();

            int c43 = lines.vec3(box.maxX, getPartMinus(box.minY, box.maxY, 6), getPartMinus(box.minZ, box.maxZ, 6)).color(line).next();
            int c44 = lines.vec3(box.maxX, box.maxY, getPartMinus(box.minZ, box.maxZ, 4)).color(line).next();
            int c45 = lines.vec3(box.maxX, getPartMinus(box.minY, box.maxY, 4), box.maxZ).color(line).next();

            int c46 = lines.vec3(box.maxX, getPartPlus(box.minY, box.maxY, 6), getPartMinus(box.minZ, box.maxZ, 6)).color(line).next();
            int c47 = lines.vec3(box.maxX, getPartPlus(box.minY, box.maxY, 4), box.maxZ).color(line).next();
            int c48 = lines.vec3(box.maxX, box.minY, getPartMinus(box.minZ, box.maxZ, 4)).color(line).next();

            //5
            int c49 = lines.vec3(getPartPlus(box.minX, box.maxX, 6), box.maxY, getPartPlus(box.minZ, box.maxZ, 6)).color(line).next();
            int c50 = lines.vec3(getPartPlus(box.minX, box.maxX, 6), box.maxY, getPartMinus(box.minZ, box.maxZ, 6)).color(line).next();
            int c51 = lines.vec3(getPartMinus(box.minX, box.maxX, 6), box.maxY, getPartMinus(box.minZ, box.maxZ, 6)).color(line).next();
            int c52 = lines.vec3(getPartMinus(box.minX, box.maxX, 6), box.maxY, getPartPlus(box.minZ, box.maxZ, 6)).color(line).next();

            int c53 = lines.vec3(getPartPlus(box.minX, box.maxX, 6), box.minY, getPartPlus(box.minZ, box.maxZ, 6)).color(line).next();
            int c54 = lines.vec3(getPartPlus(box.minX, box.maxX, 6), box.minY, getPartMinus(box.minZ, box.maxZ, 6)).color(line).next();
            int c55 = lines.vec3(getPartMinus(box.minX, box.maxX, 6), box.minY, getPartMinus(box.minZ, box.maxZ, 6)).color(line).next();
            int c56 = lines.vec3(getPartMinus(box.minX, box.maxX, 6), box.minY, getPartPlus(box.minZ, box.maxZ, 6)).color(line).next();


            //1
            lines.line(c1, c2);
            lines.line(c1, c3);

            lines.line(c2, c5);

            lines.line(c4, c5);
            lines.line(c4, c6);

            lines.line(c6, c8);

            lines.line(c7, c8);
            lines.line(c7, c9);

            lines.line(c9, c11);

            lines.line(c10, c11);
            lines.line(c10, c12);

            lines.line(c12, c3);

            //2
            lines.line(c13, c14);
            lines.line(c13, c15);

            lines.line(c14, c17);

            lines.line(c16, c17);
            lines.line(c16, c18);

            lines.line(c18, c20);

            lines.line(c19, c20);
            lines.line(c19, c21);

            lines.line(c21, c23);

            lines.line(c22, c23);
            lines.line(c22, c24);

            lines.line(c24, c15);

            //3
            lines.line(c25, c26);
            lines.line(c25, c27);

            lines.line(c26, c29);

            lines.line(c28, c29);
            lines.line(c28, c30);

            lines.line(c30, c32);

            lines.line(c31, c32);
            lines.line(c31, c33);

            lines.line(c33, c35);

            lines.line(c34, c35);
            lines.line(c34, c36);

            lines.line(c36, c27);

            //4
            lines.line(c37, c38);
            lines.line(c37, c39);

            lines.line(c38, c41);

            lines.line(c40, c41);
            lines.line(c40, c42);

            lines.line(c42, c44);

            lines.line(c43, c44);
            lines.line(c43, c45);

            lines.line(c45, c47);

            lines.line(c46, c47);
            lines.line(c46, c48);

            lines.line(c48, c39);

            //5
            lines.line(c49, c6);
            lines.line(c49, c30);

            lines.line(c50, c32);
            lines.line(c50, c18);

            lines.line(c51, c20);
            lines.line(c51, c44);

            lines.line(c52, c42);
            lines.line(c52, c8);

            //6
            lines.line(c53, c27);
            lines.line(c53, c3);

            lines.line(c54, c36);
            lines.line(c54, c15);

            lines.line(c55, c24);
            lines.line(c55, c48);

            lines.line(c56, c12);
            lines.line(c56, c39);
        }
    }

    private static void box4(Render3DEvent event, Box box, Color side, Color line, ShapeMode shapeMode){
        Mesh lines = event.renderer.lines;
        Mesh triangles = event.renderer.triangles;
        if (shapeMode == ShapeMode.Lines || shapeMode == ShapeMode.Both){
            int center = lines.vec3(getCenter(box.minX, box.maxX), box.maxY, getCenter(box.minZ, box.maxZ)).color(line).next();
            int center2 = lines.vec3(getCenter(box.minX, box.maxX), box.minY, getCenter(box.minZ, box.maxZ)).color(line).next();

            int a1 = lines.vec3(getCenter(box.minX, box.maxX), box.maxY, box.minZ).color(line).next();
            int a2 = lines.vec3(getCenter(box.minX, box.maxX), box.maxY, box.maxZ).color(line).next();

            int a3 = lines.vec3(box.minX, box.maxY, getCenter(box.minZ, box.maxZ)).color(line).next();
            int a4 = lines.vec3(box.maxX, box.maxY, getCenter(box.minZ, box.maxZ)).color(line).next();

            int b1 = lines.vec3(getPartPlus(box.minX, box.maxX, 8), box.maxY, getPartPlus(box.minZ, box.maxZ, 8)).color(line).next();
            int b2 = lines.vec3(getPartPlus(box.minX, box.maxX, 8), box.maxY, getPartMinus(box.minZ, box.maxZ, 8)).color(line).next();
            int b3 = lines.vec3(getPartMinus(box.minX, box.maxX, 8), box.maxY, getPartMinus(box.minZ, box.maxZ, 8)).color(line).next();
            int b4 = lines.vec3(getPartMinus(box.minX, box.maxX, 8), box.maxY, getPartPlus(box.minZ, box.maxZ, 8)).color(line).next();

            int c1 = lines.vec3(getPartPlus(box.minX, box.maxX, 8, 3), box.maxY, getPartPlus(box.minZ, box.maxZ, 8, 2)).color(line).next();
            int c2 = lines.vec3(getPartPlus(box.minX, box.maxX, 8, 2), box.maxY, getPartPlus(box.minZ, box.maxZ, 8, 3)).color(line).next();

            int c3 = lines.vec3(getPartPlus(box.minX, box.maxX, 8, 2), box.maxY, getPartPlus(box.minZ, box.maxZ, 8, 5)).color(line).next();
            int c4 = lines.vec3(getPartPlus(box.minX, box.maxX, 8, 3), box.maxY, getPartPlus(box.minZ, box.maxZ, 8, 6)).color(line).next();

            int c5 = lines.vec3(getPartPlus(box.minX, box.maxX, 8, 5), box.maxY, getPartPlus(box.minZ, box.maxZ, 8, 6)).color(line).next();
            int c6 = lines.vec3(getPartPlus(box.minX, box.maxX, 8, 6), box.maxY, getPartPlus(box.minZ, box.maxZ, 8, 5)).color(line).next();

            int c7 = lines.vec3(getPartPlus(box.minX, box.maxX, 8, 6), box.maxY, getPartPlus(box.minZ, box.maxZ, 8, 3)).color(line).next();
            int c8 = lines.vec3(getPartPlus(box.minX, box.maxX, 8, 5), box.maxY, getPartPlus(box.minZ, box.maxZ, 8, 2)).color(line).next();


            lines.line(center, a1);
            lines.line(center, a2);
            lines.line(center, a3);
            lines.line(center, a4);

            lines.line(b1, center);
            lines.line(b2, center);
            lines.line(b3, center);
            lines.line(b4, center);

            lines.line(a1, b1);
            lines.line(a1, b4);

            lines.line(a2, b2);
            lines.line(a2, b3);

            lines.line(a3, b1);
            lines.line(a3, b2);

            lines.line(a4, b3);
            lines.line(a4, b4);


            lines.line(c1, a1);
            lines.line(c1, b1);
            lines.line(c1, center);

            lines.line(c2, a3);
            lines.line(c2, b1);
            lines.line(c2, center);

            lines.line(c3, a3);
            lines.line(c3, b2);
            lines.line(c3, center);

            lines.line(c4, a2);
            lines.line(c4, b2);
            lines.line(c4, center);

            lines.line(c5, a2);
            lines.line(c5, b3);
            lines.line(c5, center);

            lines.line(c6, a4);
            lines.line(c6, b3);
            lines.line(c6, center);

            lines.line(c7, a4);
            lines.line(c7, b4);
            lines.line(c7, center);

            lines.line(c8, a1);
            lines.line(c8, b4);
            lines.line(c8, center);

            lines.line(a1, center2);
            lines.line(a2, center2);
            lines.line(a3, center2);
            lines.line(a4, center2);

            lines.line(b1, center2);
            lines.line(b2, center2);
            lines.line(b3, center2);
            lines.line(b4, center2);
        }
        if (shapeMode == ShapeMode.Sides || shapeMode == ShapeMode.Both){
            int center = triangles.vec3(getCenter(box.minX, box.maxX), box.maxY, getCenter(box.minZ, box.maxZ)).color(side).next();
            int center2 = triangles.vec3(getCenter(box.minX, box.maxX), box.minY, getCenter(box.minZ, box.maxZ)).color(side).next();

            int a1 = triangles.vec3(getCenter(box.minX, box.maxX), box.maxY, box.minZ).color(side).next();
            int a2 = triangles.vec3(getCenter(box.minX, box.maxX), box.maxY, box.maxZ).color(side).next();

            int a3 = triangles.vec3(box.minX, box.maxY, getCenter(box.minZ, box.maxZ)).color(side).next();
            int a4 = triangles.vec3(box.maxX, box.maxY, getCenter(box.minZ, box.maxZ)).color(side).next();

            int b1 = triangles.vec3(getPartPlus(box.minX, box.maxX, 8), box.maxY, getPartPlus(box.minZ, box.maxZ, 8)).color(side).next();
            int b2 = triangles.vec3(getPartPlus(box.minX, box.maxX, 8), box.maxY, getPartMinus(box.minZ, box.maxZ, 8)).color(side).next();
            int b3 = triangles.vec3(getPartMinus(box.minX, box.maxX, 8), box.maxY, getPartMinus(box.minZ, box.maxZ, 8)).color(side).next();
            int b4 = triangles.vec3(getPartMinus(box.minX, box.maxX, 8), box.maxY, getPartPlus(box.minZ, box.maxZ, 8)).color(side).next();

            int c1 = triangles.vec3(getPartPlus(box.minX, box.maxX, 8, 3), box.maxY, getPartPlus(box.minZ, box.maxZ, 8, 2)).color(side).next();
            int c2 = triangles.vec3(getPartPlus(box.minX, box.maxX, 8, 2), box.maxY, getPartPlus(box.minZ, box.maxZ, 8, 3)).color(side).next();

            int c3 = triangles.vec3(getPartPlus(box.minX, box.maxX, 8, 2), box.maxY, getPartPlus(box.minZ, box.maxZ, 8, 5)).color(side).next();
            int c4 = triangles.vec3(getPartPlus(box.minX, box.maxX, 8, 3), box.maxY, getPartPlus(box.minZ, box.maxZ, 8, 6)).color(side).next();

            int c5 = triangles.vec3(getPartPlus(box.minX, box.maxX, 8, 5), box.maxY, getPartPlus(box.minZ, box.maxZ, 8, 6)).color(side).next();
            int c6 = triangles.vec3(getPartPlus(box.minX, box.maxX, 8, 6), box.maxY, getPartPlus(box.minZ, box.maxZ, 8, 5)).color(side).next();

            int c7 = triangles.vec3(getPartPlus(box.minX, box.maxX, 8, 6), box.maxY, getPartPlus(box.minZ, box.maxZ, 8, 3)).color(side).next();
            int c8 = triangles.vec3(getPartPlus(box.minX, box.maxX, 8, 5), box.maxY, getPartPlus(box.minZ, box.maxZ, 8, 2)).color(side).next();

            triangles.quad(a1, c1, center, c8);
            triangles.quad(b1, c2, center, c1);
            triangles.quad(a3, c3, center, c2);
            triangles.quad(b2, c4, center, c3);
            triangles.quad(a2, c5, center, c4);
            triangles.quad(b3, c6, center, c5);
            triangles.quad(a4, c7, center, c6);
            triangles.quad(b4, c8, center, c7);

            triangles.quad(a1, b1, center2, a1);
            triangles.quad(b1, a3, center2, b1);
            triangles.quad(a3, b2, center2, a3);
            triangles.quad(b2, a2, center2, b2);
            triangles.quad(a2, b3, center2, a2);
            triangles.quad(b3, a4, center2, b3);
            triangles.quad(a4, b4, center2, a4);
            triangles.quad(b4, a1, center2, b4);
        }
    }

    static double addition = 0.0;



    public static void playerBox1(Render3DEvent event, Box box, Color side, Color line, ShapeMode shapeMode){
        Mesh lines = event.renderer.lines;

        if (shapeMode == ShapeMode.Lines || shapeMode == ShapeMode.Both){
            int ba1 = lines.vec3(box.minX, box.minY, box.minZ).color(line).next();
            int ba2 = lines.vec3(box.minX, box.minY, box.maxZ).color(line).next();
            int ba3 = lines.vec3(box.maxX, box.minY, box.maxZ).color(line).next();
            int ba4 = lines.vec3(box.maxX, box.minY, box.minZ).color(line).next();

            int bb1 = lines.vec3(box.minX, getPartPlus(box.minY, box.maxY, 10, 1), box.minZ).color(line).next();
            int bb2 = lines.vec3(box.minX, getPartPlus(box.minY, box.maxY, 10, 1), box.maxZ).color(line).next();
            int bb3 = lines.vec3(box.maxX, getPartPlus(box.minY, box.maxY, 10, 1), box.maxZ).color(line).next();
            int bb4 = lines.vec3(box.maxX, getPartPlus(box.minY, box.maxY, 10, 1), box.minZ).color(line).next();


            int iba1 = lines.vec3(getPartPlus(box.minX, box.maxX, 8, 1), box.minY, getPartPlus(box.minZ, box.maxZ, 8, 1)).color(line).next();
            int iba2 = lines.vec3(getPartPlus(box.minX, box.maxX, 8, 1), box.minY, getPartPlus(box.minZ, box.maxZ, 8, 7)).color(line).next();
            int iba3 = lines.vec3(getPartPlus(box.minX, box.maxX, 8, 7), box.minY, getPartPlus(box.minZ, box.maxZ, 8, 7)).color(line).next();
            int iba4 = lines.vec3(getPartPlus(box.minX, box.maxX, 8, 7), box.minY, getPartPlus(box.minZ, box.maxZ, 8, 1)).color(line).next();

            //1

            int pb1 = lines.vec3(getPartPlus(box.minX, box.maxX, 10, 1), getPartPlus(box.minY, box.maxY, 10, 1), box.minZ).color(line).next();
            int pb2 = lines.vec3(box.minX, getPartPlus(box.minY, box.maxY, 10, 1), getPartPlus(box.minZ, box.maxZ, 10, 1)).color(line).next();

            int pb3 = lines.vec3(box.minX, getPartPlus(box.minY, box.maxY, 10, 1), getPartPlus(box.minZ, box.maxZ, 10, 9)).color(line).next();
            int pb4 = lines.vec3(getPartPlus(box.minX, box.maxX, 10, 1), getPartPlus(box.minY, box.maxY, 10, 1), box.maxZ).color(line).next();

            int pb5 = lines.vec3(getPartPlus(box.minX, box.maxX, 10, 9), getPartPlus(box.minY, box.maxY, 10, 1), box.maxZ).color(line).next();
            int pb6 = lines.vec3(box.maxX, getPartPlus(box.minY, box.maxY, 10, 1), getPartPlus(box.minZ, box.maxZ, 10, 9)).color(line).next();

            int pb7 = lines.vec3(box.maxX, getPartPlus(box.minY, box.maxY, 10, 1), getPartPlus(box.minZ, box.maxZ, 10, 1)).color(line).next();
            int pb8 = lines.vec3(getPartPlus(box.minX, box.maxX, 10, 9), getPartPlus(box.minY, box.maxY, 10, 1), box.minZ).color(line).next();


            int pt1 = lines.vec3(getPartPlus(box.minX, box.maxX, 10, 1), getPartPlus(box.minY, box.maxY, 10, 9), box.minZ).color(line).next();
            int pt2 = lines.vec3(box.minX, getPartPlus(box.minY, box.maxY, 10, 9), getPartPlus(box.minZ, box.maxZ, 10, 1)).color(line).next();

            int pt3 = lines.vec3(box.minX, getPartPlus(box.minY, box.maxY, 10, 9), getPartPlus(box.minZ, box.maxZ, 10, 9)).color(line).next();
            int pt4 = lines.vec3(getPartPlus(box.minX, box.maxX, 10, 1), getPartPlus(box.minY, box.maxY, 10, 9), box.maxZ).color(line).next();

            int pt5 = lines.vec3(getPartPlus(box.minX, box.maxX, 10, 9), getPartPlus(box.minY, box.maxY, 10, 9), box.maxZ).color(line).next();
            int pt6 = lines.vec3(box.maxX, getPartPlus(box.minY, box.maxY, 10, 9), getPartPlus(box.minZ, box.maxZ, 10, 9)).color(line).next();

            int pt7 = lines.vec3(box.maxX, getPartPlus(box.minY, box.maxY, 10, 9), getPartPlus(box.minZ, box.maxZ, 10, 1)).color(line).next();
            int pt8 = lines.vec3(getPartPlus(box.minX, box.maxX, 10, 9), getPartPlus(box.minY, box.maxY, 10, 9), box.minZ).color(line).next();

            lines.line(ba1, iba1);
            lines.line(ba2, iba2);
            lines.line(ba3, iba3);
            lines.line(ba4, iba4);

            lines.line(pb1, pt1);
            lines.line(pb2, pt2);
            lines.line(pb3, pt3);
            lines.line(pb4, pt4);
            lines.line(pb5, pt5);
            lines.line(pb6, pt6);
            lines.line(pb7, pt7);
            lines.line(pb8, pt8);

            lines.line(ba1, bb1);
            lines.line(ba2, bb2);
            lines.line(ba3, bb3);
            lines.line(ba4, bb4);

            lines.line(ba1, ba2);
            lines.line(ba2, ba3);
            lines.line(ba3, ba4);
            lines.line(ba4, ba1);

            lines.line(iba1, iba2);
            lines.line(iba2, iba3);
            lines.line(iba3, iba4);
            lines.line(iba4, iba1);


            lines.line(bb1, bb2);
            lines.line(bb2, bb3);
            lines.line(bb3, bb4);
            lines.line(bb4, bb1);

            int ta1 = lines.vec3(box.minX, box.maxY, box.minZ).color(line).next();
            int ta2 = lines.vec3(box.minX, box.maxY, box.maxZ).color(line).next();
            int ta3 = lines.vec3(box.maxX, box.maxY, box.maxZ).color(line).next();
            int ta4 = lines.vec3(box.maxX, box.maxY, box.minZ).color(line).next();

            int tb1 = lines.vec3(box.minX, getPartPlus(box.minY, box.maxY, 10, 9), box.minZ).color(line).next();
            int tb2 = lines.vec3(box.minX, getPartPlus(box.minY, box.maxY, 10, 9), box.maxZ).color(line).next();
            int tb3 = lines.vec3(box.maxX, getPartPlus(box.minY, box.maxY, 10, 9), box.maxZ).color(line).next();
            int tb4 = lines.vec3(box.maxX, getPartPlus(box.minY, box.maxY, 10, 9), box.minZ).color(line).next();

            int ita1 = lines.vec3(getPartPlus(box.minX, box.maxX, 8, 1), box.maxY, getPartPlus(box.minZ, box.maxZ, 8, 1)).color(line).next();
            int ita2 = lines.vec3(getPartPlus(box.minX, box.maxX, 8, 1), box.maxY, getPartPlus(box.minZ, box.maxZ, 8, 7)).color(line).next();
            int ita3 = lines.vec3(getPartPlus(box.minX, box.maxX, 8, 7), box.maxY, getPartPlus(box.minZ, box.maxZ, 8, 7)).color(line).next();
            int ita4 = lines.vec3(getPartPlus(box.minX, box.maxX, 8, 7), box.maxY, getPartPlus(box.minZ, box.maxZ, 8, 1)).color(line).next();

            lines.line(ta1, ita1);
            lines.line(ta2, ita2);
            lines.line(ta3, ita3);
            lines.line(ta4, ita4);

            lines.line(ta1, tb1);
            lines.line(ta2, tb2);
            lines.line(ta3, tb3);
            lines.line(ta4, tb4);


            lines.line(ta1, ta2);
            lines.line(ta2, ta3);
            lines.line(ta3, ta4);
            lines.line(ta4, ta1);

            lines.line(tb1, tb2);
            lines.line(tb2, tb3);
            lines.line(tb3, tb4);
            lines.line(tb4, tb1);

            lines.line(ita1, ita2);
            lines.line(ita2, ita3);
            lines.line(ita3, ita4);
            lines.line(ita4, ita1);
        }
    }

    private static void box5(Render3DEvent event, Box box, Color side, Color line, ShapeMode shapeMode){
        Mesh lines = event.renderer.lines;
        Mesh triangles = event.renderer.triangles;

        if (shapeMode == ShapeMode.Lines || shapeMode == ShapeMode.Both){
            int a1 = lines.vec3(box.minX, box.minY, box.minZ).color(line).next();
            int a2 = lines.vec3(box.minX, box.maxY, box.minZ).color(line).next();

            int a3 = lines.vec3(box.minX, box.minY, box.maxZ).color(line).next();
            int a4 = lines.vec3(box.minX, box.maxY, box.maxZ).color(line).next();

            int a5 = lines.vec3(box.maxX, box.minY, box.maxZ).color(line).next();
            int a6 = lines.vec3(box.maxX, box.maxY, box.maxZ).color(line).next();

            int a7 = lines.vec3(box.maxX, box.minY, box.minZ).color(line).next();
            int a8 = lines.vec3(box.maxX, box.maxY, box.minZ).color(line).next();

            int c1 = lines.vec3(box.minX, getCenter(box.minY, box.maxY), box.minZ).color(line).next();
            int c2 = lines.vec3(box.minX, getCenter(box.minY, box.maxY), box.maxZ).color(line).next();

            int c3 = lines.vec3(box.maxX, getCenter(box.minY, box.maxY), box.maxZ).color(line).next();
            int c4 = lines.vec3(box.maxX, getCenter(box.minY, box.maxY), box.minZ).color(line).next();

            int c5 = lines.vec3(getCenter(box.minX, box.maxX), box.minY, box.minZ).color(line).next();
            int c6 = lines.vec3(getCenter(box.minX, box.maxX), box.maxY, box.minZ).color(line).next();

            int c7 = lines.vec3(box.minX, box.minY, getCenter(box.minZ, box.maxZ)).color(line).next();
            int c8 = lines.vec3(box.minX, box.maxY, getCenter(box.minZ, box.maxZ)).color(line).next();

            int c9 = lines.vec3(getCenter(box.minX, box.maxX), box.minY, box.maxZ).color(line).next();
            int c10 = lines.vec3(getCenter(box.minX, box.maxX), box.maxY, box.maxZ).color(line).next();

            int c11 = lines.vec3(box.maxX, box.minY, getCenter(box.minZ, box.maxZ)).color(line).next();
            int c12 = lines.vec3(box.maxX, box.maxY, getCenter(box.minZ, box.maxZ)).color(line).next();

            lines.line(a1, a2);
            lines.line(a3, a4);
            lines.line(a5, a6);
            lines.line(a7, a8);

            lines.line(a1, a3);
            lines.line(a3, a5);
            lines.line(a5, a7);
            lines.line(a7, a1);

            lines.line(a2, a4);
            lines.line(a4, a6);
            lines.line(a6, a8);
            lines.line(a8, a2);

            lines.line(c1, c5);
            lines.line(c1, c6);
            lines.line(c1, c7);
            lines.line(c1, c8);

            lines.line(c2, c7);
            lines.line(c2, c8);
            lines.line(c2, c9);
            lines.line(c2, c10);

            lines.line(c3, c9);
            lines.line(c3, c10);
            lines.line(c3, c11);
            lines.line(c3, c12);

            lines.line(c4, c11);
            lines.line(c4, c12);
            lines.line(c4, c5);
            lines.line(c4, c6);

            lines.line(c8, c6);
            lines.line(c8, c10);

            lines.line(c12, c6);
            lines.line(c12, c10);

            lines.line(c7, c5);
            lines.line(c7, c9);

            lines.line(c11, c5);
            lines.line(c11, c9);
        }
        if (shapeMode == ShapeMode.Sides || shapeMode == ShapeMode.Both){
            int a1 = triangles.vec3(box.minX, box.minY, box.minZ).color(side).next();
            int a2 = triangles.vec3(box.minX, box.maxY, box.minZ).color(side).next();

            int a3 = triangles.vec3(box.minX, box.minY, box.maxZ).color(side).next();
            int a4 = triangles.vec3(box.minX, box.maxY, box.maxZ).color(side).next();

            int a5 = triangles.vec3(box.maxX, box.minY, box.maxZ).color(side).next();
            int a6 = triangles.vec3(box.maxX, box.maxY, box.maxZ).color(side).next();

            int a7 = triangles.vec3(box.maxX, box.minY, box.minZ).color(side).next();
            int a8 = triangles.vec3(box.maxX, box.maxY, box.minZ).color(side).next();

            int c1 = triangles.vec3(box.minX, getCenter(box.minY, box.maxY), box.minZ).color(side).next();
            int c2 = triangles.vec3(box.minX, getCenter(box.minY, box.maxY), box.maxZ).color(side).next();

            int c3 = triangles.vec3(box.maxX, getCenter(box.minY, box.maxY), box.maxZ).color(side).next();
            int c4 = triangles.vec3(box.maxX, getCenter(box.minY, box.maxY), box.minZ).color(side).next();

            int c5 = triangles.vec3(getCenter(box.minX, box.maxX), box.minY, box.minZ).color(side).next();
            int c6 = triangles.vec3(getCenter(box.minX, box.maxX), box.maxY, box.minZ).color(side).next();

            int c7 = triangles.vec3(box.minX, box.minY, getCenter(box.minZ, box.maxZ)).color(side).next();
            int c8 = triangles.vec3(box.minX, box.maxY, getCenter(box.minZ, box.maxZ)).color(side).next();

            int c9 = triangles.vec3(getCenter(box.minX, box.maxX), box.minY, box.maxZ).color(side).next();
            int c10 = triangles.vec3(getCenter(box.minX, box.maxX), box.maxY, box.maxZ).color(side).next();

            int c11 = triangles.vec3(box.maxX, box.minY, getCenter(box.minZ, box.maxZ)).color(side).next();
            int c12 = triangles.vec3(box.maxX, box.maxY, getCenter(box.minZ, box.maxZ)).color(side).next();

            triangles.quad(c5, a1, c1, c5);
            triangles.quad(c5, a7, c4, c5);

            triangles.quad(c6, a2, c1, c6);
            triangles.quad(c6, a8, c4, c6);

            triangles.quad(c7, a1, c1, c7);
            triangles.quad(c7, a3, c2, c7);

            triangles.quad(c8, a2, c1, c8);
            triangles.quad(c8, a4, c2, c8);

            triangles.quad(c9, a3, c2, c9);
            triangles.quad(c9, a5, c3, c9);

            triangles.quad(c10, a4, c2, c10);
            triangles.quad(c10, a6, c3, c10);

            triangles.quad(c11, a5, c3, c11);
            triangles.quad(c11, a7, c4, c11);

            triangles.quad(c12, a6, c3, c12);
            triangles.quad(c12, a8, c4, c12);

            triangles.quad(c6, a2, c8, c6);
            triangles.quad(c6, a8, c12, c6);

            triangles.quad(c10, a4, c8, c10);
            triangles.quad(c10, a6, c12, c10);

            triangles.quad(c5, a1, c7, c5);
            triangles.quad(c5, a7, c11, c5);

            triangles.quad(c9, a3, c7, c9);
            triangles.quad(c9, a5, c11, c9);
        }
    }


    private static double getCenter(double min, double max){
        return min + (max - min) / 2;
    }

    private static double getPartPlus(double min, double max, int i){
        return min + (max - min) / i;
    }

    private static double getPartPlus(double min, double max, int i, int q){
        return min + ((max - min) / i) * q;
    }

    private static double getPartMinus(double min, double max, int i){
        return max - (max - min) / i;
    }

    private static double getPartMinus(double min, double max, int i, int q){
        return max - ((max - min) / i) * q;
    }

    public enum BoxRendererMethod {
        Box,
        Patter1,
        Patter2,
        Patter3,
        Pattern4,
        Pattern5
    }
}
