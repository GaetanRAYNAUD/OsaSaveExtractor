package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.game.Religion;
import fr.osallek.eu4parser.model.save.Save;
import fr.osallek.eu4parser.model.save.country.SaveCountry;
import fr.osallek.eu4parser.model.save.province.SaveProvince;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.DoubleConsumer;
import java.util.function.Predicate;

public class SaveDTO {

    private final String id;

    private final String name;

    private final String provinceImage;

    private final String colorsImage;

    private final LocalDate date;

    private final int nbProvinces;

    private final List<TeamDTO> teams;

    private final SortedSet<ProvinceDTO> provinces = new ConcurrentSkipListSet<>();

    private final SortedSet<SimpleProvinceDTO> oceansProvinces = new ConcurrentSkipListSet<>();

    private final SortedSet<SimpleProvinceDTO> lakesProvinces = new ConcurrentSkipListSet<>();

    private final SortedSet<SimpleProvinceDTO> impassableProvinces = new ConcurrentSkipListSet<>();

    private final List<CountryDTO> countries;

    private final List<AreaDTO> areas;

    private final List<AdvisorDTO> advisors;

    private final List<CultureDTO> cultures;

    private final List<ReligionDTO> religions;

    private final HreDTO hre;

    private final CelestialEmpireDTO celestialEmpire;

    private final List<InstitutionDTO> institutions;

    private final DiplomacyDTO diplomacy;

    private final List<NamedImageLocalisedDTO> buildings;

    private final List<NamedImageLocalisedDTO> advisorTypes;

    private final List<TradeGoodDTO> tradeGoods;

    public SaveDTO(Save save, String provinceImage, String colorsImage, Map<String, Religion> religions, DoubleConsumer percentCountriesConsumer) {
        this.provinceImage = provinceImage;
        this.colorsImage = colorsImage;
        this.id = UUID.randomUUID().toString();
        this.name = save.getName();
        this.date = save.getDate();
        this.nbProvinces = Collections.max(save.getGame().getProvinces().keySet()); //Get the greatest link
        this.teams = CollectionUtils.isNotEmpty(save.getTeams()) ? save.getTeams().stream().map(TeamDTO::new).toList() : null;

        save.getProvinces().values().forEach(province -> {
            if (province.isImpassable()) {
                this.impassableProvinces.add(new SimpleProvinceDTO(province));
            } else if (province.isOcean()) {
                this.oceansProvinces.add(new SimpleProvinceDTO(province));
            } else if (province.isLake()) {
                this.lakesProvinces.add(new SimpleProvinceDTO(province));
            } else if (province.getHistory() != null) {
                this.provinces.add(new ProvinceDTO(province));
            }
        });

        this.areas = save.getAreas().values().stream().map(AreaDTO::new).toList();
        this.advisors = save.getAdvisors().values().stream().map(AdvisorDTO::new).toList();

        AtomicInteger i = new AtomicInteger();
        this.countries = save.getCountries().values().parallelStream().filter(Predicate.not(SaveCountry::isObserver)).map(country -> {
            CountryDTO countryDTO = new CountryDTO(save, country, save.getDiplomacy());

            countryDTO.getHistory().stream().filter(history -> StringUtils.isNotBlank(history.getChangedTagFrom())).forEach(history -> {
                this.provinces.stream()
                              .filter(province -> province.isOwnerAt(history.getDate(), history.getChangedTagFrom()))
                              .forEach(province -> province.addOwner(history.getDate(), countryDTO.getTag()));
                this.provinces.stream() //Add owner when inheriting from decision
                              .filter(province -> CollectionUtils.isNotEmpty(province.getHistory())
                                                  && province.getHistory().stream().anyMatch(h -> h.getDate().equals(history.getDate())
                                                                                                  && country.getTag().equals(h.getFakeOwner())))
                              .forEach(province -> province.addOwner(history.getDate(), countryDTO.getTag()));
            });

            i.getAndIncrement();
            percentCountriesConsumer.accept((double) i.get() / save.getCountries().values().size());

            return countryDTO;
        }).toList();

        this.cultures = save.getGame().getCultures().stream().map(c -> new CultureDTO(save, c)).toList();
        this.religions = save.getReligions()
                             .getReligions()
                             .values()
                             .stream()
                             .filter(r -> religions.containsKey(r.getName()))
                             .map(r -> new ReligionDTO(save, r, religions.get(r.getName())))
                             .toList();
        this.hre = new HreDTO(save.getHre());
        this.celestialEmpire = new CelestialEmpireDTO(save.getCelestialEmpire());
        this.institutions = save.getGame()
                                .getInstitutions()
                                .stream()
                                .filter(institution -> save.getInstitutions().isAvailable(institution))
                                .map(institution -> {
                                    SaveProvince origin = save.getInstitutions().getOrigin(institution);
                                    return origin != null ? new InstitutionDTO(save, institution, save.getInstitutions().getOrigin(institution).getId())
                                                          : new InstitutionDTO(save, institution, 0);
                                })
                                .toList();
        this.diplomacy = new DiplomacyDTO(save.getDiplomacy());
        this.buildings = save.getGame()
                             .getBuildings()
                             .stream()
                             .map(building -> new NamedImageLocalisedDTO(save.getGame().getLocalisation("building_" + building.getName()),
                                                                         building.getImage(), building.getName()))
                             .toList();
        this.advisorTypes = save.getGame()
                                .getAdvisors()
                                .stream()
                                .map(advisor -> new NamedImageLocalisedDTO(save.getGame().getLocalisation(advisor.getName()), advisor.getDefaultImage(),
                                                                           advisor.getName()))
                                .toList();
        this.tradeGoods = save.getGame().getTradeGoods().stream().map(tradeGood -> new TradeGoodDTO(save, tradeGood)).toList();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getProvinceImage() {
        return provinceImage;
    }

    public String getColorsImage() {
        return colorsImage;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getNbProvinces() {
        return nbProvinces;
    }

    public List<TeamDTO> getTeams() {
        return teams;
    }

    public SortedSet<ProvinceDTO> getProvinces() {
        return provinces;
    }

    public SortedSet<SimpleProvinceDTO> getOceansProvinces() {
        return oceansProvinces;
    }

    public SortedSet<SimpleProvinceDTO> getLakesProvinces() {
        return lakesProvinces;
    }

    public SortedSet<SimpleProvinceDTO> getImpassableProvinces() {
        return impassableProvinces;
    }

    public List<CountryDTO> getCountries() {
        return countries;
    }

    public List<AreaDTO> getAreas() {
        return areas;
    }

    public List<AdvisorDTO> getAdvisors() {
        return advisors;
    }

    public List<CultureDTO> getCultures() {
        return cultures;
    }

    public List<ReligionDTO> getReligions() {
        return religions;
    }

    public HreDTO getHre() {
        return hre;
    }

    public CelestialEmpireDTO getCelestialEmpire() {
        return celestialEmpire;
    }

    public List<InstitutionDTO> getInstitutions() {
        return institutions;
    }

    public DiplomacyDTO getDiplomacy() {
        return diplomacy;
    }

    public List<NamedImageLocalisedDTO> getBuildings() {
        return buildings;
    }

    public List<NamedImageLocalisedDTO> getAdvisorTypes() {
        return advisorTypes;
    }

    public List<TradeGoodDTO> getTradeGoods() {
        return tradeGoods;
    }
}
