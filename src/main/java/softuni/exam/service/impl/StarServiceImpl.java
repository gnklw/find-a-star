package softuni.exam.service.impl;

import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.models.dto.StarDTO;
import softuni.exam.models.entity.Constellation;
import softuni.exam.models.entity.Star;
import softuni.exam.repository.ConstellationRepository;
import softuni.exam.repository.StarRepository;
import softuni.exam.service.StarService;
import softuni.exam.util.StarType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StarServiceImpl implements StarService {

    private static final String PATH = "src/main/resources/files/json/stars.json";

    private final StarRepository starRepository;
    private final ConstellationRepository constellationRepository;
    private final ModelMapper modelMapper;
    private final Gson gson;

    public StarServiceImpl(StarRepository starRepository, ConstellationRepository constellationRepository, ModelMapper modelMapper, Gson gson) {
        this.starRepository = starRepository;
        this.constellationRepository = constellationRepository;
        this.modelMapper = modelMapper;
        this.gson = gson;
    }

    @Override
    public boolean areImported() {
        return this.starRepository.count() > 0;
    }

    @Override
    public String readStarsFileContent() throws IOException {
        return Files.readString(Path.of(PATH));
    }


    @Override
    public String importStars() throws IOException {
        List<StarDTO> stars = Arrays
                .stream(gson.fromJson(readStarsFileContent(), StarDTO[].class))
                .collect(Collectors.toList());
        StringBuilder output = new StringBuilder();
        for (StarDTO star : stars) {
            Optional<Constellation> constellation = constellationRepository.findById(star.getConstellation());
            if (starRepository.findByName(star.getName()).isPresent()
                    || constellation.isEmpty()) {
                output
                        .append("Invalid star")
                        .append(System.lineSeparator());
            } else {
                output
                        .append(String.format("Successfully imported star %s - %.2f light years",
                                star.getName(), star.getLightYears()))
                        .append(System.lineSeparator());
                Star starEntity = modelMapper.map(star, Star.class);
                starEntity.setConstellation(constellation.get());
                starRepository.save(starEntity);
            }
        }

        return output.toString().trim();
    }

    @Override
    public String exportStars() {
        StringBuilder output = new StringBuilder();
        starRepository
                .findAllByStarTypeAndObserversIsEmptyOrderByLightYearsAsc(StarType.RED_GIANT)
                .forEach(e -> output
                        .append(String.format("Star: %s", e.getName()))
                        .append(System.lineSeparator())
                        .append(String.format(" *Distance: %.2f light years", e.getLightYears()))
                        .append(System.lineSeparator())
                        .append(String.format(" **Description: %s", e.getDescription()))
                        .append(System.lineSeparator())
                        .append(String.format(" ***Constellation: %s", e.getConstellation().getName()))
                        .append(System.lineSeparator()));

        return output.toString().trim();
    }
}
