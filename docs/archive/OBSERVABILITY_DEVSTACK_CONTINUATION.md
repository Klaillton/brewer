# Observability Devstack Continuation Instructions

Use this document as the handoff for the observability chat when continuing the same validation and deployment configuration work.

## Objective

Confirm that the observability stack is deployed and validated through the Kubernetes/devstack flow, with Docker Compose treated as legacy/local-only support.

## What to verify first

1. The observability deployment path points to Kubernetes manifests as the source of truth.
2. The devstack-infra handoff exists and is the mechanism used to trigger deployment on `devserverpi`.
3. Any legacy Docker Compose usage is documented as local-only and not part of the server path.
4. The namespace, services, and ingress/resources expected by observability are aligned with the Kubernetes layout.

## Files and signals to check

- [README.md](README.md) for the Kubernetes-only deployment rule and guardrails.
- [.github/workflows/deploy-to-pi.yml](.github/workflows/deploy-to-pi.yml) for the devstack-infra repository dispatch.
- [deploy.sh](deploy.sh) for the Brewer deploy flow that mirrors the same operational model.
- [k8s/](k8s/) and the observability manifests referenced from there for the runtime topology.
- [scripts/validate-no-docker-compose-on-server.sh](scripts/validate-no-docker-compose-on-server.sh) and [scripts/validate-deployment-environment.sh](scripts/validate-deployment-environment.sh) for server-side guardrails.

## Validation sequence

1. Confirm the Kubernetes manifests are complete enough to deploy the observability stack without Compose.
2. Verify the deploy trigger path from the repo to devstack-infra is still active.
3. Check that the deployment instructions clearly forbid Compose on `devserverpi`.
4. Validate the final apply/rollout path that devstack-infra uses for the cluster.
5. Record any missing manifests, env vars, secrets, or namespace wiring needed to make the deployment reproducible.

## Expected result

- Observability deploys through the same Kubernetes-first operational model as Brewer.
- Devstack-infra is the deployment orchestrator for the server.
- Docker Compose remains only as a local reference, not as a server deploy path.

## Definition of done

- The deployment path is documented.
- The validation and guardrails are explicit.
- The observability chat can continue without re-discovering the Brewer cleanup context.
