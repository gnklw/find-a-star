package softuni.exam.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.models.dto.AstronomerDTO;
import softuni.exam.models.dto.AstronomersDTO;
import softuni.exam.models.entity.Astronomer;
import softuni.exam.models.entity.Star;
import softuni.exam.repository.AstronomerRepository;
import softuni.exam.repository.StarRepository;
import softuni.exam.service.AstronomerService;
import softuni.exam.util.XMLParser;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AstronomerServiceImpl implements AstronomerService {

    private static final String PATH = "src/main/resources/files/xml/astronomers.xml";

    private final AstronomerRepository astronomerRepository;
    private final ModelMapper modelMapper;
    private final XMLParser xmlParser;
    private final StarRepository starRepository;

    public AstronomerServiceImpl(AstronomerRepository astronomerRepository, ModelMapper modelMapper, XMLParser xmlParser, StarRepository starRepository) {
        this.astronomerRepository = astronomerRepository;
        this.modelMapper = modelMapper;
        this.xmlParser = xmlParser;
        this.starRepository = starRepository;
    }

    @Override
    public boolean areImported() {
        return this.astronomerRepository.count() > 0;
    }

    @Override
    public String readAstronomersFromFile() throws IOException {
        return Files.readString(Path.of(PATH));
    }

    @Override
    public String importAstronomers() throws IOException, JAXBException {
        List<AstronomerDTO> astronomers = xmlParser
                .fromFile(Path.of(PATH).toFile(), AstronomersDTO.class)
                .getAstronomers();
        StringBuilder output = new StringBuilder();
        for (AstronomerDTO astronomer : astronomers) {
            Optional<Star> star = this.starRepository.findById(astronomer.getObservingStar());
            if (astronomerRepository.findByFirstNameAndLastName(astronomer.getFirstName(), astronomer.getLastName()).isPresent()
                    || star.isEmpty()) {
                output
                        .append("Invalid astronomer")
                        .append(System.lineSeparator());
            } else {
                output
                        .append(String.format("Successfully imported astronomer %s %s - %.2f",
                                astronomer.getFirstName(), astronomer.getLastName(), astronomer.getSalary()))
                        .append(System.lineSeparator());

                Astronomer astronomerEntity = modelMapper.map(astronomer, Astronomer.class);
                astronomerEntity.setObservingStar(star.get());
                astronomerRepository.save(astronomerEntity);
            }
        }

        return output.toString().trim();
    }
}
