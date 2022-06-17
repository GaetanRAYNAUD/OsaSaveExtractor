package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.clausewitzparser.common.ClausewitzUtils;
import fr.osallek.eu4parser.common.NumbersUtils;
import fr.osallek.eu4parser.model.game.Culture;
import fr.osallek.eu4parser.model.save.country.Losses;
import fr.osallek.eu4parser.model.save.country.PowerProjection;
import fr.osallek.eu4parser.model.save.country.PowerSpent;
import fr.osallek.eu4parser.model.save.country.SaveCountry;
import fr.osallek.eu4parser.model.save.diplomacy.DatableRelation;
import fr.osallek.eu4parser.model.save.diplomacy.Diplomacy;
import fr.osallek.eu4parser.model.save.diplomacy.Subsidies;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.stream.Collectors;

public class CountryDTO {

    private final String tag;

    private final String customName;

    private final List<String> players;

    private final Integer greatPowerRank;

    private final Map<String, LocalDate> flags;

    private final Map<String, LocalDate> hiddenFlags;

    private final Map<String, Double> variables;

    private final ColorsDTO colors;

    private final int milTech;

    private final int admTech;

    private final int dipTech;

    private final List<EstateDTO> estates;

    private final Pair<String, Double> hegemony;

    private final List<String> rivals;

    private final double powerProjection;

    private final List<LoanDTO> loans;

    private final Map<String, Integer> ideaGroups;

    private final GovernmentDTO government;

    private final List<LeaderDTO> leader;

    private final List<Integer> advisors;

    //    private final List<Monarch> monarchs;

    //    private final Heir heir;

    //    private final Queen queen;

    private final Map<PowerSpent, Integer> admPowerSpent;

    private final Map<PowerSpent, Integer> dipPowerSpent;

    private final Map<PowerSpent, Integer> milPowerSpent;

    private final List<CustomNationalIdeaDTO> customNationalIdeas;

    //    private final Missions countryMissions; //Todo completed

    private final SortedMap<Integer, Integer> incomeStatistics;

    private final SortedMap<Integer, Integer> nationSizeStatistics;

    private final SortedMap<Integer, Integer> scoreStatistics;

    private final SortedMap<Integer, Integer> inflationStatistics;

    private final List<String> alliances;

    private final List<String> guarantees;

    private final List<String> guarantedBy;

    private final String knowledgeSharing;

    private final String knowledgeSharingBy;

    private final Map<String, Double> subsidies;

    private final Map<String, Double> subsidiesBy;

    private final List<String> royalMarriages;

    private final List<String> supportIndependence;

    private final List<String> supportIndependenceBy;

    private final List<String> transferTradePowers;

    private final List<String> transferTradePowersBy;

    private final List<String> warReparations;

    private final List<String> atWarWith;

    private final String warReparationsBy;

    private final List<String> warnings;

    private final List<String> warningsBy;

    private final Double prestige;

    private final Integer stability;

    private final Double inflation;

    private final Double corruption;

    private final String religion;

    private final String primaryCulture;

    private final List<String> acceptedCultures;

    private final Double treasury;

    private final Double armyProfessionalism;

    private final Integer absolutism;

    private final Integer mercantilism;

    private final Double navyTradition;

    private final Double armyTradition;

    private final LocalDate lastBankrupt;

    private final int manpower;

    private final int maxManpower;

    private final int sailors;

    private final int maxSailors;

    private final Map<Losses, Integer> losses;

    private final double innovativeness;

    public CountryDTO(SaveCountry country, Diplomacy diplomacy) {
        this.tag = country.getTag();
        this.customName = country.getCustomName();
        this.players = CollectionUtils.isEmpty(country.getPlayers()) ? null : country.getPlayers().stream().map(ClausewitzUtils::removeQuotes).toList();
        this.greatPowerRank = country.getGreatPowerRank();
        this.flags = country.getFlags() == null ? null : country.getFlags().getAll();
        this.hiddenFlags = country.getHiddenFlags() == null ? null : country.getHiddenFlags().getAll();
        this.variables = country.getVariables() == null ? null : country.getVariables().getAll();
        this.colors = new ColorsDTO(country.getColors());
        this.milTech = country.getTech().getMil();
        this.admTech = country.getTech().getAdm();
        this.dipTech = country.getTech().getDip();
        this.estates = country.getEstates().stream().map(EstateDTO::new).toList();
        this.hegemony = country.getHegemon() == null ? null : Pair.of(country.getHegemon().hegemon().getName(), country.getHegemon().getProgress());
        this.rivals = country.getRivals().keySet().stream().toList();
        this.powerProjection = country.getPowerProjections().stream().mapToDouble(PowerProjection::getCurrent).sum();
        this.loans = country.getLoans().stream().map(LoanDTO::new).toList();
        this.ideaGroups = country.getIdeaGroups().getIdeaGroupsNames();
        this.government = country.getGovernment() == null ? null : new GovernmentDTO(country.getGovernment());
        this.leader = MapUtils.isEmpty(country.getLeaders()) ? null : country.getLeaders().values().stream().map(LeaderDTO::new).toList();
        this.advisors = country.getActiveAdvisors().keySet().stream().toList();
        this.admPowerSpent = country.getAdmPowerSpent() == null ? null : country.getAdmPowerSpent().getPowerSpent();
        this.dipPowerSpent = country.getDipPowerSpent() == null ? null : country.getDipPowerSpent().getPowerSpent();
        this.milPowerSpent = country.getMilPowerSpent() == null ? null : country.getMilPowerSpent().getPowerSpent();
        this.customNationalIdeas = CollectionUtils.isEmpty(country.getCustomNationalIdeas()) ? null : country.getCustomNationalIdeas()
                                                                                                             .stream()
                                                                                                             .map(CustomNationalIdeaDTO::new)
                                                                                                             .toList();
        this.incomeStatistics = country.getIncomeStatistics();
        this.nationSizeStatistics = country.getNationSizeStatistics();
        this.scoreStatistics = country.getScoreStatistics();
        this.inflationStatistics = country.getInflationStatistics();
        this.alliances = diplomacy.getAlliances()
                                  .stream()
                                  .filter(r -> r.getFirst().getTag().equals(this.tag))
                                  .map(DatableRelation::getSecond)
                                  .map(SaveCountry::getTag)
                                  .collect(Collectors.toList());
        this.alliances.addAll(diplomacy.getAlliances()
                                       .stream()
                                       .filter(r -> r.getSecond().getTag().equals(this.tag))
                                       .map(DatableRelation::getFirst)
                                       .map(SaveCountry::getTag)
                                       .toList());
        this.guarantees = diplomacy.getGuarantees()
                                   .stream()
                                   .filter(r -> r.getFirst().getTag().equals(this.tag))
                                   .map(DatableRelation::getSecond)
                                   .map(SaveCountry::getTag)
                                   .collect(Collectors.toList());
        this.guarantedBy = diplomacy.getGuarantees()
                                    .stream()
                                    .filter(r -> r.getSecond().getTag().equals(this.tag))
                                    .map(DatableRelation::getFirst)
                                    .map(SaveCountry::getTag)
                                    .collect(Collectors.toList());
        this.knowledgeSharing = diplomacy.getKnowledgeSharing()
                                         .stream()
                                         .filter(r -> r.getFirst().getTag().equals(this.tag))
                                         .findFirst()
                                         .map(DatableRelation::getSecond)
                                         .map(SaveCountry::getTag)
                                         .orElse(null);
        this.knowledgeSharingBy = diplomacy.getKnowledgeSharing()
                                           .stream()
                                           .filter(r -> r.getSecond().getTag().equals(this.tag))
                                           .findFirst()
                                           .map(DatableRelation::getSecond)
                                           .map(SaveCountry::getTag)
                                           .orElse(null);
        this.subsidies = diplomacy.getSubsidies()
                                  .stream()
                                  .filter(r -> r.getFirst().getTag().equals(this.tag))
                                  .collect(Collectors.toMap(s -> s.getSecond().getTag(), Subsidies::getAmount));
        this.subsidiesBy = diplomacy.getSubsidies()
                                    .stream()
                                    .filter(r -> r.getSecond().getTag().equals(this.tag))
                                    .collect(Collectors.toMap(s -> s.getFirst().getTag(), Subsidies::getAmount));
        this.royalMarriages = diplomacy.getRoyalMarriage()
                                       .stream()
                                       .filter(r -> r.getFirst().getTag().equals(this.tag))
                                       .map(DatableRelation::getSecond)
                                       .map(SaveCountry::getTag)
                                       .collect(Collectors.toList());
        this.royalMarriages.addAll(diplomacy.getRoyalMarriage()
                                            .stream()
                                            .filter(r -> r.getSecond().getTag().equals(this.tag))
                                            .map(DatableRelation::getFirst)
                                            .map(SaveCountry::getTag)
                                            .toList());
        this.supportIndependence = diplomacy.getSupportIndependence()
                                            .stream()
                                            .filter(r -> r.getFirst().getTag().equals(this.tag))
                                            .map(DatableRelation::getSecond)
                                            .map(SaveCountry::getTag)
                                            .collect(Collectors.toList());
        this.supportIndependenceBy = diplomacy.getSupportIndependence()
                                              .stream()
                                              .filter(r -> r.getSecond().getTag().equals(this.tag))
                                              .map(DatableRelation::getFirst)
                                              .map(SaveCountry::getTag)
                                              .collect(Collectors.toList());
        this.transferTradePowers = diplomacy.getTransferTradePowers()
                                            .stream()
                                            .filter(r -> r.getFirst().getTag().equals(this.tag))
                                            .map(DatableRelation::getSecond)
                                            .map(SaveCountry::getTag)
                                            .collect(Collectors.toList());
        this.transferTradePowersBy = diplomacy.getTransferTradePowers()
                                              .stream()
                                              .filter(r -> r.getSecond().getTag().equals(this.tag))
                                              .map(DatableRelation::getFirst)
                                              .map(SaveCountry::getTag)
                                              .collect(Collectors.toList());
        this.warReparations = diplomacy.getWarReparations()
                                       .stream()
                                       .filter(r -> r.getFirst().getTag().equals(this.tag))
                                       .map(DatableRelation::getSecond)
                                       .map(SaveCountry::getTag)
                                       .collect(Collectors.toList());
        this.warReparationsBy = diplomacy.getWarReparations()
                                         .stream()
                                         .filter(r -> r.getSecond().getTag().equals(this.tag))
                                         .findFirst()
                                         .map(DatableRelation::getSecond)
                                         .map(SaveCountry::getTag)
                                         .orElse(null);
        this.warnings = diplomacy.getWarnings()
                                 .stream()
                                 .filter(r -> r.getFirst().getTag().equals(this.tag))
                                 .map(DatableRelation::getSecond)
                                 .map(SaveCountry::getTag)
                                 .collect(Collectors.toList());
        this.warningsBy = diplomacy.getWarnings()
                                   .stream()
                                   .filter(r -> r.getSecond().getTag().equals(this.tag))
                                   .map(DatableRelation::getFirst)
                                   .map(SaveCountry::getTag)
                                   .collect(Collectors.toList());
        this.atWarWith = country.getActiveWars()
                                .stream()
                                .map(war -> war.getOtherSide(country))
                                .map(Map::entrySet)
                                .flatMap(Collection::stream)
                                .map(Map.Entry::getKey)
                                .map(SaveCountry::getTag)
                                .distinct()
                                .toList();
        this.prestige = country.getPrestige();
        this.corruption = country.getCorruption();
        this.stability = country.getStability();
        this.inflation = country.getInflation();
        this.religion = country.getReligionName();
        this.primaryCulture = country.getPrimaryCulture() == null ? null : country.getPrimaryCulture().getName();
        this.acceptedCultures = country.getAcceptedCultures().stream().map(Culture::getName).toList();
        this.treasury = country.getTreasury();
        this.armyProfessionalism = country.getArmyProfessionalism();
        this.absolutism = country.getAbsolutism();
        this.mercantilism = country.getMercantilism();
        this.navyTradition = country.getNavyTradition();
        this.armyTradition = country.getArmyTradition();
        this.lastBankrupt = country.lastBankrupt();
        this.maxManpower = NumbersUtils.doubleToInt(NumbersUtils.doubleOrDefault(country.getMaxManpower()) * 1000);
        this.manpower = NumbersUtils.doubleToInt(NumbersUtils.doubleOrDefault(country.getManpower()) * 1000);
        this.maxSailors = NumbersUtils.doubleToInt(NumbersUtils.doubleOrDefault(country.getMaxSailors()) * 1000);
        this.sailors = NumbersUtils.doubleToInt(NumbersUtils.doubleOrDefault(country.getSailors()) * 1000);
        this.losses = country.getLosses();
        this.innovativeness = NumbersUtils.doubleOrDefault(country.getInnovativeness());
    }

    public String getTag() {
        return tag;
    }

    public String getCustomName() {
        return customName;
    }

    public List<String> getPlayers() {
        return players;
    }

    public Integer getGreatPowerRank() {
        return greatPowerRank;
    }

    public Map<String, LocalDate> getFlags() {
        return flags;
    }

    public Map<String, LocalDate> getHiddenFlags() {
        return hiddenFlags;
    }

    public Map<String, Double> getVariables() {
        return variables;
    }

    public ColorsDTO getColors() {
        return colors;
    }

    public int getMilTech() {
        return milTech;
    }

    public int getAdmTech() {
        return admTech;
    }

    public int getDipTech() {
        return dipTech;
    }

    public List<EstateDTO> getEstates() {
        return estates;
    }

    public Pair<String, Double> getHegemony() {
        return hegemony;
    }

    public List<String> getRivals() {
        return rivals;
    }

    public double getPowerProjection() {
        return powerProjection;
    }

    public List<LoanDTO> getLoans() {
        return loans;
    }

    public Map<String, Integer> getIdeaGroups() {
        return ideaGroups;
    }

    public GovernmentDTO getGovernment() {
        return government;
    }

    public List<LeaderDTO> getLeader() {
        return leader;
    }

    public List<Integer> getAdvisors() {
        return advisors;
    }

    public Map<PowerSpent, Integer> getAdmPowerSpent() {
        return admPowerSpent;
    }

    public Map<PowerSpent, Integer> getDipPowerSpent() {
        return dipPowerSpent;
    }

    public Map<PowerSpent, Integer> getMilPowerSpent() {
        return milPowerSpent;
    }

    public List<CustomNationalIdeaDTO> getCustomNationalIdeas() {
        return customNationalIdeas;
    }

    public SortedMap<Integer, Integer> getIncomeStatistics() {
        return incomeStatistics;
    }

    public SortedMap<Integer, Integer> getNationSizeStatistics() {
        return nationSizeStatistics;
    }

    public SortedMap<Integer, Integer> getScoreStatistics() {
        return scoreStatistics;
    }

    public SortedMap<Integer, Integer> getInflationStatistics() {
        return inflationStatistics;
    }

    public List<String> getAlliances() {
        return alliances;
    }

    public List<String> getGuarantees() {
        return guarantees;
    }

    public List<String> getGuarantedBy() {
        return guarantedBy;
    }

    public String getKnowledgeSharing() {
        return knowledgeSharing;
    }

    public String getKnowledgeSharingBy() {
        return knowledgeSharingBy;
    }

    public Map<String, Double> getSubsidies() {
        return subsidies;
    }

    public Map<String, Double> getSubsidiesBy() {
        return subsidiesBy;
    }

    public List<String> getRoyalMarriages() {
        return royalMarriages;
    }

    public List<String> getSupportIndependence() {
        return supportIndependence;
    }

    public List<String> getSupportIndependenceBy() {
        return supportIndependenceBy;
    }

    public List<String> getTransferTradePowers() {
        return transferTradePowers;
    }

    public List<String> getTransferTradePowersBy() {
        return transferTradePowersBy;
    }

    public List<String> getWarReparations() {
        return warReparations;
    }

    public List<String> getAtWarWith() {
        return atWarWith;
    }

    public String getWarReparationsBy() {
        return warReparationsBy;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public List<String> getWarningsBy() {
        return warningsBy;
    }

    public Double getPrestige() {
        return prestige;
    }

    public Integer getStability() {
        return stability;
    }

    public Double getInflation() {
        return inflation;
    }

    public Double getCorruption() {
        return corruption;
    }

    public String getReligion() {
        return religion;
    }

    public String getPrimaryCulture() {
        return primaryCulture;
    }

    public List<String> getAcceptedCultures() {
        return acceptedCultures;
    }

    public Double getTreasury() {
        return treasury;
    }

    public Double getArmyProfessionalism() {
        return armyProfessionalism;
    }

    public Integer getAbsolutism() {
        return absolutism;
    }

    public Integer getMercantilism() {
        return mercantilism;
    }

    public Double getNavyTradition() {
        return navyTradition;
    }

    public Double getArmyTradition() {
        return armyTradition;
    }

    public LocalDate getLastBankrupt() {
        return lastBankrupt;
    }

    public int getManpower() {
        return manpower;
    }

    public int getMaxManpower() {
        return maxManpower;
    }

    public int getSailors() {
        return sailors;
    }

    public int getMaxSailors() {
        return maxSailors;
    }

    public Map<Losses, Integer> getLosses() {
        return losses;
    }

    public double getInnovativeness() {
        return innovativeness;
    }
}