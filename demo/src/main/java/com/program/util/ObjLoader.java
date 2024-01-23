/* Name: Andrew Manga
 ** Date: June 14, 2023
 ** Class: ICS4U1 - J. Radulovic
 ** Assignment: Culminating
 ** Purpose: Create a JavaFX program to simulate and illustrate the changes in population in an ecosystem.
 */

package com.program.util;

import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class ObjLoader {

    public static MeshView loadObj(String path) {
        TriangleMesh mesh = new TriangleMesh(VertexFormat.POINT_NORMAL_TEXCOORD);
        java.util.ArrayList<String> lines = readTextFile(path);
        lines.forEach((line) -> {
            if (line != null) {
                line = line.trim();
                char indicator = line.charAt(0);
                switch (indicator) {
                    case '#': // Skip all lines beginning with these characters (not important)
                    case 'g':
                    case 'l':
                    case 'o':
                    case 's':
                        break;
                    case 'f':
                        String[] faces = line.replace("f", "").trim().split(" ");
                        for (String face : faces) {
                            String[] temp = face.split("/");
                            mesh.getFaces().addAll(Integer.parseInt(temp[0]) - 1);
                            mesh.getFaces().addAll(Integer.parseInt(temp[2]) - 1);
                            mesh.getFaces().addAll(Integer.parseInt(temp[1]) - 1);
                        }
                        break;
                    case 'v':
                        switch (line.charAt(1)) {
                            // Geometric vertices
                            case ' ':
                                String[] verts = line.replace("v", "").trim().split(" ");
                                for (String vert : verts) {
                                    mesh.getPoints().addAll(Float.parseFloat(vert));
                                }
                                break;
                            // Texture coordinates
                            case 't':
                                String[] texts = line.replace("vt", "").trim().split(" ");
                                for (String text : texts) {
                                    mesh.getTexCoords().addAll(Float.parseFloat(text));
                                }
                                break;
                            // Vertex normals
                            case 'n':
                                String[] norms = line.replace("vn", "").trim().split(" ");
                                for (String norm : norms) {
                                    mesh.getNormals().addAll(Float.parseFloat(norm));
                                }
                                break;
                            case 'p':
                                break;
                            default:
                                System.out.println("loadObj: Bad vertex: " + line);
                                break;
                        }
                        break;
                }
            }
        });
        return new MeshView(mesh);
    }

    private static ArrayList<String> readTextFile(String path) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            java.util.ArrayList<String> lines = new ArrayList<>();
            lines.add(br.readLine());
            while (lines.get(lines.size() - 1) != null) {
                lines.add(br.readLine());
            }
            return lines;
        } catch (Exception e) {
            System.out.println("Exception thrown when reading `" + path + "`");
        }
        return null;
    }

}
