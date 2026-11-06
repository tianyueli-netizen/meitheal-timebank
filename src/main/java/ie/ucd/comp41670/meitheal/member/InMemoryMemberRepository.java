package ie.ucd.comp41670.meitheal.member;

import ie.ucd.comp41670.meitheal.contract.MemberRepository;
import ie.ucd.comp41670.meitheal.domain.MemberId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
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
    public Optional<Member> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return storage.values().stream()
                .filter(m -> m.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public Member registerMember(String name, String email) {
        MemberId newId = new MemberId("MEM-" + UUID.randomUUID().toString().substring(0, 8));
        Member member = new Member(newId, name, email);
        save(member);
        return member;
    }

    @Override
    public List<Member> findAll() {
        // R12: 使用 List.copyOf 返回不可变视图
        return List.copyOf(storage.values());
    }

    @Override
    public int count() {
        return storage.size();
    }
}