package fr.osallek.osasaveextractor.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.osallek.eu4parser.common.ZipUtils;
import fr.osallek.osasaveextractor.OsaSaveExtractorApplication;
import fr.osallek.osasaveextractor.common.exception.ServerException;
import fr.osallek.osasaveextractor.config.ApplicationProperties;
import fr.osallek.osasaveextractor.controller.object.DataAssetDTO;
import fr.osallek.osasaveextractor.controller.object.ErrorObject;
import fr.osallek.osasaveextractor.service.object.save.SaveDTO;
import fr.osallek.osasaveextractor.service.object.server.ServerSave;
import fr.osallek.osasaveextractor.service.object.server.UploadResponseDTO;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class ServerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerService.class);

    private final ApplicationProperties properties;

    private final ObjectMapper objectMapper;

    private final RestTemplate restTemplate;

    public ServerService(ApplicationProperties properties, ObjectMapper objectMapper, RestTemplate restTemplate) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    public SortedSet<ServerSave> getSaves() {
        ResponseEntity<List<ServerSave>> response = this.restTemplate.exchange(
                this.properties.getServerUrl() + "/api/save/user/" + OsaSaveExtractorApplication.ID, HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});

        if (!HttpStatus.OK.equals(response.getStatusCode())) {
            LOGGER.error("An error occurred while retrieving saves from server: {}", response.getStatusCode());
        }

        SortedSet<ServerSave> saves = new TreeSet<>(Comparator.comparing(ServerSave::creationDate).reversed());

        if (CollectionUtils.isNotEmpty(response.getBody())) {
            saves.addAll(response.getBody());
        }

        return saves;
    }

    public CompletableFuture<UploadResponseDTO> uploadData(SaveDTO save) throws JsonProcessingException {
        try {
            ResponseEntity<String> response = new RestTemplate().postForEntity(this.properties.getServerUrl() + "/api/save", save, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                return CompletableFuture.failedFuture(new ServerException(this.objectMapper.readValue(response.getBody(), ErrorObject.class).getError()));
            }

            return CompletableFuture.completedFuture(this.objectMapper.readValue(response.getBody(), UploadResponseDTO.class));
        } catch (HttpClientErrorException e) {
            return CompletableFuture.failedFuture(new ServerException(this.objectMapper.readValue(e.getResponseBodyAsString(), ErrorObject.class).getError()));
        }
    }

    public CompletableFuture<Boolean> uploadAssets(List<Path> assets, Path root, String id) throws IOException {
        Path zip = root.resolve("assets.zip");

        ZipUtils.zipFolder(root, zip, assets::contains);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("assets", new FileSystemResource(zip));
        body.add("data", new DataAssetDTO(OsaSaveExtractorApplication.ID, id));

        try {
            ResponseEntity<String> response = this.restTemplate.postForEntity(this.properties.getServerUrl() + "/api/data",
                                                                              new HttpEntity<>(body, headers), String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                return CompletableFuture.failedFuture(new ServerException(this.objectMapper.readValue(response.getBody(), ErrorObject.class).getError()));
            }

            return CompletableFuture.completedFuture(true);
        } catch (HttpClientErrorException e) {
            return CompletableFuture.failedFuture(new ServerException(this.objectMapper.readValue(e.getResponseBodyAsString(), ErrorObject.class).getError()));
        }
    }
}
