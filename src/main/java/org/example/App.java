package org.example;

import org.example.dao.ProveedorDao;
import org.example.models.Proveedor;

public class App {

    public static void main(String[] args) {

        ProveedorDao proveedorDao = new ProveedorDao();

        Proveedor proveedor = new Proveedor(
                "PR001",
                "Laura",
                "García",
                "Calle Paz 10, Vitoria");

        proveedorDao.create(proveedor);

        System.out.println("Proveedor creado:");
        System.out.println(proveedor);

        Proveedor encontrado = proveedorDao.find("PR001");

        System.out.println("Proveedor encontrado:");
        System.out.println(encontrado);
    }
}