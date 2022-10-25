package com.argpro.argentinaprograma.controllers.GestionUsuarioController;


import com.argpro.argentinaprograma.controllers.GenericoController.BaseControllerImplem;
import com.argpro.argentinaprograma.models.GestionUsuarioModel.UsuarioModel;
import com.argpro.argentinaprograma.repositories.GestionUsuarioRepository.IUsuarioRepository;
import com.argpro.argentinaprograma.services.GestionUsuarioServices.UsuarioService;
import com.argpro.argentinaprograma.services.GestionUsuarioServices.UsuarioServiceImple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/gestionusuario/usuario") //, produces="text/plain", value = ("/api/gestionusuario/usuario"),headers=("content-type=multipart/form-data")
public class UsuarioController extends BaseControllerImplem<UsuarioModel, UsuarioServiceImple> {

    Logger logger = LoggerFactory.getLogger(UsuarioController.class);

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private IUsuarioRepository iUsuarioRepository;

    UsuarioModel imagenModel = new UsuarioModel();

    @PostMapping("query")
    public ResponseEntity<?> agregarRolAusuario(@RequestParam("useremail") String useremail, @RequestParam("rolname") String rolname){

       usuarioService.roleAusuario(useremail, rolname);
       return ResponseEntity.ok().build();
       
    }

    //usaremos este metodo solo ADMINS
    //USUARIO IMAGEN SUBIR Y EXTRAER
    @PostMapping("/upload")
    public ResponseEntity<?> uplaodImage(@RequestParam("file") MultipartFile file) throws IOException { //MultipartFile


        try{
            //byte[] base64 = sobremiService.compressBytes(file.getBytes());
            byte[] base64 = usuarioService.compressBytes(file.getBytes());

            if(base64.length > 0){

                UsuarioModel usuarioModel = new UsuarioModel(); // ORIGINAL
                usuarioModel.setTituloimg(file.getOriginalFilename());
                usuarioModel.setImagenbyte(base64);

                // para pasarlo por el body entity de response entity
                this.imagenModel.setTituloimg(usuarioModel.getTituloimg());
                this.imagenModel.setImagenbyte(usuarioModel.getImagenbyte());

            }

            return new ResponseEntity<UsuarioModel>(this.imagenModel, HttpStatus.OK);
        }catch(Exception ex){
            return ResponseEntity.badRequest().body("ERRoOR... "+ex);
        }
    }

    //traer imagen por el nombre de la imagen (tituloimg)
    @GetMapping("/get/{imageName}")
    public ResponseEntity<?> getImage(@PathVariable("imageName") String imageName) throws IOException {

        Optional<UsuarioModel> retrievedImage = iUsuarioRepository.findByTituloimg(imageName);

        UsuarioModel usuarioModelimg = new UsuarioModel();
        usuarioModelimg.setTituloimg(retrievedImage.get().getTituloimg());
        usuarioModelimg.setImagenbyte(retrievedImage.get().getImagenbyte());


        return ResponseEntity.ok(usuarioModelimg);


    }

    //traer imagen por el nombre de la imagen (tituloimg)
    @GetMapping("/getImagebyemail/{useremail}")
    public ResponseEntity<?> getImageByEmail(@PathVariable("useremail") String useremail) throws IOException {

        Optional<UsuarioModel> retrievedImage = iUsuarioRepository.findByNombreOrEmail(useremail, useremail);

        byte[] base64 = usuarioService.decompressBytes(retrievedImage.get().getImagenbyte());

        return ResponseEntity.ok(retrievedImage.get().getImagenbyte());

    }
    
}
    
@Data
class agregarRolAusuarioForm {
    private String useremail;
    private String rolname;
}