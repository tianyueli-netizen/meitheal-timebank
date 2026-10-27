package ie.ucd.comp41670.meitheal.member;

import ie.ucd.comp41670.meitheal.contract.MemberRepository;
import ie.ucd.comp41670.meitheal.domain.MemberId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryMemberRepository implements MemberRepository {
    private final Map<MemberId, Member> storage = new ConcurrentHashMap<>();

    @Override
    public void save(Member member) {
        Objects.requireNonNull(member, "Member to save cannot be null");
        storage.put(member.getId(), member);
    }

    @Override
    public Optional<Member> findById(MemberId id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Member> findAll() {
        // R12 核心实现：使用 List.copyOf 或 unmodifiableList 彻底杜绝集合泄露
        return List.copyOf(storage.values());
    }

    @Override
    public int count() {
        return storage.size();
    }
}