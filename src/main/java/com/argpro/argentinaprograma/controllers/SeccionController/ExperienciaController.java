package com.argpro.argentinaprograma.controllers.SeccionController;

import com.argpro.argentinaprograma.controllers.GenericoController.BaseControllerImplem;
import com.argpro.argentinaprograma.models.DTOs.GetCardlistDTO;
import com.argpro.argentinaprograma.models.DTOs.ImagencardPreviewDatabaseDTO;
import com.argpro.argentinaprograma.models.DTOs.RespuestaModel;
import com.argpro.argentinaprograma.models.SeccionModel.experiencia.ExperienciaModel;
import com.argpro.argentinaprograma.models.SeccionModel.experiencia.Experienciacard;
import com.argpro.argentinaprograma.models.SeccionModel.sobremi.SobremiModel;
import com.argpro.argentinaprograma.repositories.SeccionRepository.IExperienciaRepository;
import com.argpro.argentinaprograma.repositories.SeccionRepository.IExperienciacardRepository;
import com.argpro.argentinaprograma.services.SeccionService.ExperienciaService;
import com.argpro.argentinaprograma.services.SeccionService.ExperienciaServiceImple;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.json.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/seccion/experiencia")
public class ExperienciaController extends BaseControllerImplem<ExperienciaModel, ExperienciaServiceImple> {

    @Autowired
    ExperienciaService experienciaService;

    @Autowired
    IExperienciaRepository iExperienciaRepository;

    //repositorio de la cards
    @Autowired
    IExperienciacardRepository iExperienciacardRepository;
    Logger logger = LoggerFactory.getLogger(ExperienciaController.class);


    // traer info de cards
    @GetMapping("/getcardlist")
    public ResponseEntity<?> getCardlist() throws IOException {
        List<GetCardlistDTO> cardlistsDTO = new ArrayList<>();

        try {
            var findallTest = iExperienciacardRepository.findAll().stream().collect(Collectors.toList());
            if(findallTest.isEmpty()){
                return ResponseEntity.ok().body("EXPERIENCE CARD ESTA VACIO");
            }
            AtomicInteger cont = new AtomicInteger();
            findallTest.stream().forEach(elemento -> {
                logger.info(String.valueOf(elemento.getImagencard()));
                cardlistsDTO.add(new GetCardlistDTO(Math.toIntExact(elemento.getId()), elemento.getTitulocard(),
                        elemento.getSubtitulocard(), new String(elemento.getImagencard(), StandardCharsets.UTF_8)));
                cont.getAndIncrement();
            });

            System.out.println("Cuantos elementos hay en la lista " + cardlistsDTO.size());
            return ResponseEntity.ok().body(cardlistsDTO);//);

        }catch(Exception ex){
            return ResponseEntity.badRequest().body("ERROR al traer las cardlistr ... "+ex);
        }
    }


    @DeleteMapping("/deletecards")
    public ResponseEntity<?> DeleteCardlist() throws IOException {

       iExperienciacardRepository.deleteAll();

        var borrado = iExperienciacardRepository.findAll();

        if(borrado.isEmpty()){
            return ResponseEntity.ok().body("BORRADO");
        }

        return ResponseEntity.noContent().build();

    }

    //imagenes de la card
    @PostMapping("/getImgcardPreview")
    public ResponseEntity<?> getImgcardPreview(

            @RequestPart("imagencardpreview") MultipartFile imagencardpreview)
            throws IOException {  //   @RequestParam("id") Integer id,   QUITAR POR ALS DUDAS ID

        logger.info("imagencardpreview.getBytes(): "+imagencardpreview.getBytes());

        ImagencardPreviewDatabaseDTO imagencardPreviewDatabaseDTO = new ImagencardPreviewDatabaseDTO();

        try{

            //var imagencard = iExperienciacardRepository.findById(Long.valueOf(String.valueOf(id)));  //QUITAR POR ALS DUDAS

            //convertir archivo a array para guardar en BASE DE DATOS
            byte[] imgcardpreview_db = experienciaService.compressBytes(imagencardpreview.getBytes());

            //convertir a imagen para MOSTRAR DISPLAY en angular
            byte[] imgcardpreview_ts = experienciaService.decompressBytes(imgcardpreview_db);

            //imagencard.get().setImagencard(imgcardpreview_db);  //QUITAR POR ALS DUDAS


            if(imgcardpreview_db.length > 0 || imgcardpreview_ts.length > 0){

                Experienciacard experienciacard = new Experienciacard(); //ORIGINAL

                imagencardPreviewDatabaseDTO.setImagencardPreviewdb(imgcardpreview_db);
                imagencardPreviewDatabaseDTO.setImagecardPreviewts(imgcardpreview_ts);


                var imagescardlist = iExperienciacardRepository.findAll();

                List<Experienciacard> cardlists = new ArrayList<>();
                List<String> listString = new ArrayList<>();
                List<byte[]> listByte = new ArrayList<>();

                //imagencardPreviewDatabaseDTO.setImgcardpurodb(imagescardlist);
                for(int i = 0; i < imagescardlist.size(); i++){

                    listByte.add(imagescardlist.get(i).getImagencard());

                }

                //iExperienciacardRepository.save(imagencard.get()); //QUITAR POR ALS DUDAS

                imagencardPreviewDatabaseDTO.setImgcardpurodb(listByte);
                logger.info(listByte.toString());


            }


            return new ResponseEntity<ImagencardPreviewDatabaseDTO>(imagencardPreviewDatabaseDTO, HttpStatus.OK);
        }catch(Exception ex){
            return ResponseEntity.badRequest().body("ERROR en experiencia controller ... "+ex);
        }

    }


    //desde aca las cards
    @PostMapping("/crearnuevacard")
    public ResponseEntity<?> crearcards(
            @RequestParam("titulocard") String titulocard,
            @RequestParam("subtitulocard") String subtitulocard,
            @RequestParam("imagecardPreviewts") String imagecardPreviewts
    ) throws IOException {


        logger.info(String.valueOf(imagecardPreviewts));

        Experienciacard experienciacard = new Experienciacard();
        experienciacard.setTitulocard(titulocard);
        experienciacard.setSubtitulocard(subtitulocard);
        experienciacard.setImagencard(imagecardPreviewts.getBytes(StandardCharsets.UTF_8));

        ImagencardPreviewDatabaseDTO enviarInfoAngular = new ImagencardPreviewDatabaseDTO();

        enviarInfoAngular.setImagencardString(imagecardPreviewts);

        try{

            //enviarInfoAngular.setExperienciacards(Collections.singleton(experienciacard));
            iExperienciacardRepository.save(experienciacard);

            var expcards = iExperienciacardRepository.findAll().stream().collect(Collectors.toList());

            //return ResponseEntity.ok("sera succesful");
            return new ResponseEntity<List<Experienciacard>>(expcards, HttpStatus.OK);
        }catch(Exception ex){
            return ResponseEntity.badRequest().body("ERROR en navbar controller ... "+ex);
        }

    }


    //TODO: Update titulo del card ACTUALIZAR titulo de la card ya existente



    @PostMapping("/updatetitulocard")
    public ResponseEntity<?> updatetext(
            @RequestParam("id") Integer id,
            @RequestParam("tituloeditcard") String tituloeditcard)
            throws IOException {

        SobremiModel sobremiModel = new SobremiModel();

        try{
            if(tituloeditcard.isEmpty()){
                return new ResponseEntity<>("error: titulo y subtitulo vacios", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Optional<Experienciacard> experienciacardsDatabase = iExperienciacardRepository.findById(Long.valueOf(id));
            var textosDatabase = experienciacardsDatabase.get().getTitulocard();

            if(textosDatabase.isEmpty()){
                return new ResponseEntity<>("sobremi DB estan vacios", HttpStatus.NOT_MODIFIED);
            }

            experienciacardsDatabase.get().setTitulocard(tituloeditcard);

            var guardarresultado = iExperienciacardRepository.save(experienciacardsDatabase.get());



            logger.info("que nos trae  el parametro titulo:... " + tituloeditcard);

            return new ResponseEntity<>(guardarresultado, HttpStatus.OK);

        }catch(Exception ex){
            return ResponseEntity.badRequest().body("ERROR en sobremi titulo controller ... "+ex);
        }


    }


    //TODO: updatesubtitulo del card ACTUALIZAR subtitulo de card ya existente
    //TODO: actualizar campo de SUBTITULO
    @PostMapping("/updatesubtitulocard")
    public ResponseEntity<?> updatesubtitulo(
            @RequestParam("id") String id,
            @RequestParam("subtituloeditcard") String subtituloeditcard)
            throws IOException {

        try{
            if(subtituloeditcard.isEmpty()){
                return new ResponseEntity<>("error: subtitulo vacios", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Optional<Experienciacard> experienciacardsDatabase = iExperienciacardRepository.findById(Long.valueOf(id));
            var textosDatabase = experienciacardsDatabase.get().getSubtitulocard();

            if(textosDatabase.isEmpty()){
                return new ResponseEntity<>("sobremi DB estan vacios", HttpStatus.NOT_MODIFIED);
            }

            experienciacardsDatabase.get().setSubtitulocard(subtituloeditcard);
            experienciacardsDatabase.get().setSubtitulocard(subtituloeditcard);

            var guardarresultado = iExperienciacardRepository.save(experienciacardsDatabase.get());

            logger.info("que nos trae  el parametro titulo:... " + subtituloeditcard);

            return new ResponseEntity<>(guardarresultado, HttpStatus.OK);

        }catch(Exception ex){
            return ResponseEntity.badRequest().body("ERROR en sobremi subtitulo controller ... "+ex);
        }

    }


    //ACTUALIZAR IMAGEN
    //imagenes de la card
    @PostMapping("/actualizarimg")
    public ResponseEntity<?> getImgcardPreview(
            @RequestParam("id") Integer id,
            @RequestPart("imagencardpreview") MultipartFile imagencardpreview)
            throws IOException {  //   @RequestParam("id") Integer id,   QUITAR POR ALS DUDAS ID

        logger.info("imagencardpreview.getBytes().toString(): "+imagencardpreview.getBytes().toString());

        logger.info("encode stream: "+Base64.getEncoder().encodeToString(imagencardpreview.getBytes()));

        var base64encoder = Base64.getEncoder().encodeToString(imagencardpreview.getBytes());

        //ImagencardPreviewDatabaseDTO imagencardPreviewDatabaseDTO = new ImagencardPreviewDatabaseDTO();

        try{

            var imagencard = iExperienciacardRepository.findById(Long.valueOf(id));  //QUITAR POR ALS DUDAS

            //convertir archivo a array para guardar en BASE DE DATOS
            byte[] imgcardpreview_db = experienciaService.compressBytes(imagencardpreview.getBytes());

            logger.info("Base64.getEncoder().encodeToString(imgcardpreview_db): "+Base64.getEncoder().encodeToString(imgcardpreview_db));

            //convertir a imagen para MOSTRAR DISPLAY en angular
            byte[] imgcardpreview_ts = experienciaService.decompressBytes(imgcardpreview_db);

            logger.info("imgcardpreview_ts: "+imgcardpreview_ts);

            //imagencard.get().setImagencard(Base64.getDecoder().decode(imagencardpreview.getBytes()));  //QUITAR POR ALS DUDAS
            imagencard.get().setImagencard(Base64.getEncoder().encode(imagencardpreview.getBytes()));
            //imagencard.get().setImagencard(Base64.getEncoder().encodeToString(imagencardpreview.getBytes()));


            if(imgcardpreview_db.length > 0 || imgcardpreview_ts.length > 0){

                Experienciacard experienciacard = new Experienciacard(); //ORIGINAL

                iExperienciacardRepository.save(imagencard.get()); //QUITAR POR ALS DUDAS

            }

            return new ResponseEntity<Experienciacard>(imagencard.get(), HttpStatus.OK);
        }catch(Exception ex){
            return ResponseEntity.badRequest().body("ERROR en experiencia controller ... "+ex);
        }

    }

    //ACTUALIZAR IMAGEN
    //imagenes de la card
    @DeleteMapping("/borrarcardporid/{id}")
    public ResponseEntity<?> borrarcardporid(@PathVariable("id") Integer id) throws IOException {

           RespuestaModel respuesta = new RespuestaModel("", false);
           try{
                if(!iExperienciacardRepository.existsById(id.longValue())){
                    respuesta.setMensaje("La card seleccionada no existe");
                    respuesta.setBorrado(false);
                }

               if(iExperienciacardRepository.existsById(id.longValue())){
                   iExperienciacardRepository.deleteById(id.longValue());
                   respuesta.setMensaje("La card se ha eliminado correctamente!!!");
                   respuesta.setBorrado(true);
               }

               return new ResponseEntity<RespuestaModel>(respuesta, HttpStatus.OK);

           }catch(Exception exception){
               return ResponseEntity.badRequest().body("ERROR en experiencia ... "+exception);
           }

    }

}