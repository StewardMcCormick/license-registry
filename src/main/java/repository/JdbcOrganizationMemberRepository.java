package repository;

import config.DatabaseConfig;
import exception.RepositoryException;
import model.GlobalRole;
import model.MemberRole;
import model.OrganizationMember;
import model.User;
import repository.util.RowMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JdbcOrganizationMemberRepository implements OrganizationMemberRepository {

	@Override
	public OrganizationMember create(OrganizationMember member) {
		String sql = "INSERT INTO organization_member (org_id, user_id, member_role) " +
				"VALUES (?, ?, ?) RETURNING joined_at";
		try (Connection connection = DatabaseConfig.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setObject(1, member.getOrgId());
			statement.setObject(2, member.getUserId());
			statement.setString(3, member.getMemberRole().name());
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					member.setJoinedAt(resultSet.getObject("joined_at", OffsetDateTime.class));
				}
			}
			return member;
		} catch (SQLException e) {
			throw new RepositoryException("Не удалось добавить участника организации", e);
		}
	}

	@Override
	public Optional<OrganizationMember> findByOrgAndUser(UUID orgId, UUID userId) {
		String sql = """
				SELECT
				         u.id,
				         u.email,
				         u.password_hash,
				         u.last_name,
				         u.first_name,
				         u.middle_name,
				         u.created_at,
				         u.global_role,
				         u.created_at,
				         om.org_id,
				         om.user_id,
				         om.member_role,
				         om.joined_at
				FROM organization_member om
				JOIN users u ON u.id = om.user_id
				WHERE org_id = ? AND user_id = ?
				""";
		try (Connection connection = DatabaseConfig.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setObject(1, orgId);
			statement.setObject(2, userId);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return Optional.of(mapRow(resultSet));
				}
				return Optional.empty();
			}
		} catch (SQLException e) {
			throw new RepositoryException("Не удалось найти участника организации", e);
		}
	}

	@Override
	public List<OrganizationMember> findByOrgId(UUID orgId) {
		String sql = """
				SELECT
				         u.id,
				         u.email,
				         u.password_hash,
				         u.last_name,
				         u.first_name,
				         u.middle_name,
				         u.created_at,
				         u.global_role,
				         u.created_at,
				         om.org_id,
				         om.user_id,
				         om.member_role,
				         om.joined_at
				     FROM organization_member om
				     JOIN users u ON u.id = om.user_id
				     WHERE om.org_id = ?;
				""";
		List<OrganizationMember> members = new ArrayList<>();
		try (Connection connection = DatabaseConfig.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setObject(1, orgId);
			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					members.add(mapRow(resultSet));
				}
			}
			return members;
		} catch (SQLException e) {
			throw new RepositoryException("Не удалось получить участников организации", e);
		}
	}

	@Override
	public List<OrganizationMember> findByUserId(UUID userId) {
		String sql = """
				SELECT
				     u.id,
				     u.email,
				     u.password_hash,
				     u.last_name,
				     u.first_name,
				     u.middle_name,
				     u.created_at,
				     u.global_role,
				     u.created_at,
				     om.org_id,
				     om.user_id,
				     om.member_role,
				     om.joined_at
				 FROM organization_member om
				 JOIN users u ON u.id = om.user_id
				 WHERE om.user_id = ?;
				""";
		List<OrganizationMember> members = new ArrayList<>();
		try (Connection connection = DatabaseConfig.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setObject(1, userId);
			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					members.add(mapRow(resultSet));
				}
			}
			return members;
		} catch (SQLException e) {
			throw new RepositoryException("Не удалось получить организации пользователя", e);
		}
	}

	@Override
	public void updateRole(UUID orgId, UUID userId, MemberRole memberRole) {
		String sql = "UPDATE organization_member SET member_role = ? WHERE org_id = ? AND user_id = ?";
		try (Connection connection = DatabaseConfig.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, memberRole.name());
			statement.setObject(2, orgId);
			statement.setObject(3, userId);
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new RepositoryException("Не удалось обновить роль участника", e);
		}
	}

	@Override
	public void delete(UUID orgId, UUID userId) {
		String sql = "DELETE FROM organization_member WHERE org_id = ? AND user_id = ?";
		try (Connection connection = DatabaseConfig.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setObject(1, orgId);
			statement.setObject(2, userId);
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new RepositoryException("Не удалось удалить участника организации", e);
		}
	}

	private OrganizationMember mapRow(ResultSet resultSet) throws SQLException {
		OrganizationMember member = new OrganizationMember();
		member.setOrgId(resultSet.getObject("org_id", UUID.class));
		member.setUserId(resultSet.getObject("user_id", UUID.class));
		member.setMemberRole(MemberRole.valueOf(resultSet.getString("member_role")));
		member.setJoinedAt(resultSet.getObject("joined_at", OffsetDateTime.class));
		member.setUser(RowMapper.mapUser(resultSet, ""));
		return member;
	}
}
