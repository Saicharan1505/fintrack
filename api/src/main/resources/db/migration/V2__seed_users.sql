-- Roles
INSERT INTO roles (name) VALUES
  ('ADMIN'),
  ('MANAGER'),
  ('EMPLOYEE');

-- Users (temporary plaintext passwords; we'll switch to BCrypt when we add auth)
INSERT INTO users (email, password, full_name) VALUES
  ('ada.admin@demo.local',    'changeme', 'Ada Admin'),
  ('alice.manager@demo.local','changeme', 'Alice Manager'),
  ('evan.employee@demo.local','changeme', 'Evan Employee');

-- Role mappings
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON u.email='ada.admin@demo.local' AND r.name='ADMIN';
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON u.email='alice.manager@demo.local' AND r.name='MANAGER';
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON u.email='evan.employee@demo.local' AND r.name='EMPLOYEE';
