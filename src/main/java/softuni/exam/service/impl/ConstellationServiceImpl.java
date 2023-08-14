package softuni.exam.service.impl;

import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.models.dto.ConstellationDTO;
import softuni.exam.models.entity.Constellation;
import softuni.exam.repository.ConstellationRepository;
import softuni.exam.service.ConstellationService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConstellationServiceImpl implements ConstellationService {

    private static final String PATH = "src/main/resources/files/json/constellations.json";

    private final ConstellationRepository constellationRepository;
    private final ModelMapper modelMapper;
    private final Gson gson;

    public ConstellationServiceImpl(ConstellationRepository constellationRepository, ModelMapper modelMapper, Gson gson) {
        this.constellationRepository = constellationRepository;
        this.modelMapper = modelMapper;
        this.gson = gson;
    }

    @Override
    public boolean areImported() {
        return this.constellationRepository.count() > 0;
    }

    @Override
    public String readConstellationsFromFile() throws IOException {
        return Files.readString(Path.of(PATH));
    }

    @Override
    public String importConstellations() throws IOException {
        List<ConstellationDTO> constellations = Arrays
                .stream(gson.fromJson(readConstellationsFromFile(), ConstellationDTO[].class))
                .collect(Collectors.toList());
        StringBuilder output = new StringBuilder();
        for (ConstellationDTO constellation : constellations) {
            if (constellationRepository.findByName(constellation.getName()).isPresent()) {
                output
                        .append("Invalid constellation")
                        .append(System.lineSeparator());
            } else {
                output
                        .append(String.format("Successfully imported constellation %s - %s",
                                constellation.getName(), constellation.getDescription()))
                        .append(System.lineSeparator());

                constellationRepository.save(modelMapper.map(constellation, Constellation.class));
            }
        }

        return output.toString().trim();
    }
}
