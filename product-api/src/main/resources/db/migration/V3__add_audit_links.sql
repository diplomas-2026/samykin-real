alter table audit_logs
    add column actor_user_id bigint,
    add column payout_id bigint;

update audit_logs al
set actor_user_id = u.id
from users u
where lower(u.email) = lower(al.actor_email)
  and al.actor_user_id is null;

update audit_logs al
set payout_id = p.id
from payouts p
where (
    al.action in ('CREATE_PAYOUT', 'UPDATE_PAYOUT', 'UPDATE_PAYOUT_STATUS')
    and p.payout_code is not null
    and al.details like '%' || p.payout_code || '%'
)
and al.payout_id is null;

alter table audit_logs
    add constraint fk_audit_logs_actor_user
        foreign key (actor_user_id) references users(id);

alter table audit_logs
    add constraint fk_audit_logs_payout
        foreign key (payout_id) references payouts(id);

create index idx_audit_logs_actor_user_id on audit_logs(actor_user_id);
create index idx_audit_logs_payout_id on audit_logs(payout_id);
