package ie.ucd.comp41670.meitheal.member;

import ie.ucd.comp41670.meitheal.contract.MemberRepository;
import ie.ucd.comp41670.meitheal.domain.MemberId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryMemberRepository implements MemberRepository {
    private final Map<MemberId, Member> storage = new ConcurrentHashMap<>();

    @Override
    public void save(Member member) {
        storage.put(member.getId(), member);
    }

    @Override
    public Optional<Member> findById(MemberId id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Member> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public int count() {
        return storage.size();
    }
}