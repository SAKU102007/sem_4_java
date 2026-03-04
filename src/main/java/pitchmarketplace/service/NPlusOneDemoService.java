package pitchmarketplace.service;

import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pitchmarketplace.domain.entity.OpenGame;
import pitchmarketplace.dto.NPlusOneDemoResultDto;
import pitchmarketplace.repository.OpenGameRepository;

@Service
public class NPlusOneDemoService {

    private final OpenGameRepository openGameRepository;
    private final SessionFactory sessionFactory;

    public NPlusOneDemoService(OpenGameRepository openGameRepository, jakarta.persistence.EntityManagerFactory entityManagerFactory) {
        this.openGameRepository = openGameRepository;
        this.sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
    }

    @Transactional(readOnly = true)
    public NPlusOneDemoResultDto demonstrateBadCase() {
        Statistics statistics = sessionFactory.getStatistics();
        statistics.clear();

        List<OpenGame> openGames = openGameRepository.findAll();
        long participantTotal = openGames.stream()
                .mapToLong(openGame -> openGame.getParticipants().size())
                .sum();

        return new NPlusOneDemoResultDto(
                "bad_n_plus_one",
                openGames.size(),
                participantTotal,
                statistics.getPrepareStatementCount()
        );
    }

    @Transactional(readOnly = true)
    public NPlusOneDemoResultDto demonstrateSolvedCase() {
        Statistics statistics = sessionFactory.getStatistics();
        statistics.clear();

        List<OpenGame> openGames = openGameRepository.findAllWithParticipantsEntityGraph();
        long participantTotal = openGames.stream()
                .mapToLong(openGame -> openGame.getParticipants().size())
                .sum();

        return new NPlusOneDemoResultDto(
                "solved_with_entity_graph",
                openGames.size(),
                participantTotal,
                statistics.getPrepareStatementCount()
        );
    }
}
